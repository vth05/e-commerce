package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.ProductVariantCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductVariantUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductImageResponse;
import com.e_commerce.e_commerce.dto.response.ProductVariantResponse;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.entity.ProductImage;
import com.e_commerce.e_commerce.entity.ProductVariant;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.ProductImageMapper;
import com.e_commerce.e_commerce.mapper.ProductVariantMapper;
import com.e_commerce.e_commerce.repository.ProductImageRepository;
import com.e_commerce.e_commerce.repository.ProductRepository;
import com.e_commerce.e_commerce.repository.ProductVariantRepository;
import com.e_commerce.e_commerce.util.ProductVariantUtils;
import com.e_commerce.e_commerce.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ProductVariantService {
    ProductVariantRepository productVariantRepository;
    ProductVariantMapper productVariantMapper;
    ProductRepository productRepository;
    ProductVariantImageService productVariantImageService;
    ProductImageRepository productImageRepository;
    ProductImageMapper productImageMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponse createProductVariant(ProductVariantCreationRequest request, String productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        if (!product.isActive()) {
            throw new AppException(ErrorCode.PRODUCT_IS_NOT_ACTIVE);
        }
        ProductVariant productVariant = productVariantMapper.toProductVariant(request);
        productVariant.setProduct(product);
        productVariant.setProductVariantName(ProductVariantUtils.generateProductVariantName(
                product.getName(),
                productVariant.getCpu(),
                productVariant.getRam(),
                productVariant.getStorage(),
                productVariant.getGpu(),
                productVariant.getScreenSize(),
                productVariant.getScreenResolution(),
                productVariant.getRefreshRateHz()
        ));
        productVariant.setSku(ProductVariantUtils.generateSku(product.getCode(), productVariant.getColor(), productVariant.getRam(), productVariant.getStorage()));
        try {
            productVariant = productVariantRepository.save(productVariant);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.SKU_ALREADY_EXISTED);
        }
        return productVariantMapper.toProductVariantResponse(productVariant);
    }

    public ProductVariantResponse getProductVariantById(String productVariantId) {
        boolean isAdmin = SecurityUtils.isAdmin();
        ProductVariant productVariant;
        if (isAdmin) {
            productVariant = productVariantRepository.findById(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        } else {
            productVariant = productVariantRepository.findByIdAndActiveTrue(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
            productRepository.findByIdAndActiveTrue(productVariant.getProduct().getId()).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        }
        return productVariantMapper.toProductVariantResponse(productVariant);
    }

    public Page<ProductVariantResponse> getProductVariantsByProductId(String productId, int page, int size, String sortBy, String sortDir) {
        boolean isAdmin = SecurityUtils.isAdmin();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariant> productVariants;
        if (isAdmin) {
            productVariants = productVariantRepository.findAllByProductId(productId, pageable);
        } else {
            productRepository.findByIdAndActiveTrue(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            productVariants = productVariantRepository.findAllByProductIdAndActiveTrue(productId, pageable);
        }

        return productVariants.map(productVariant -> productVariantMapper.toProductVariantResponse(productVariant));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponse updateProductVariant(ProductVariantUpdateRequest request, String productVariantId) {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        productVariantMapper.updateProductVariant(productVariant, request);
        productVariant.setProductVariantName(ProductVariantUtils.generateProductVariantName(
                productVariant.getProduct().getName(),
                productVariant.getCpu(),
                productVariant.getRam(),
                productVariant.getStorage(),
                productVariant.getGpu(),
                productVariant.getScreenSize(),
                productVariant.getScreenResolution(),
                productVariant.getRefreshRateHz()
        ));
        productVariant.setSku(ProductVariantUtils.generateSku(productVariant.getProduct().getCode(), productVariant.getColor(), productVariant.getRam(), productVariant.getStorage()));
        try {
            productVariant = productVariantRepository.save(productVariant);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.SKU_ALREADY_EXISTED);
        }
        return productVariantMapper.toProductVariantResponse(productVariant);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponse deactivateProductVariant(String productVariantId) {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        productVariant.setActive(false);
        return productVariantMapper.toProductVariantResponse(productVariantRepository.save(productVariant));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductImageResponse uploadVariantImage(String productVariantId, MultipartFile file) throws IOException {
        ProductVariant variant = productVariantRepository.findById(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        String imageUrl = productVariantImageService.uploadImage(file);

        ProductImage newImage = ProductImage.builder()
                .imageUrl(imageUrl)
                .productVariant(variant)
                .build();

        return productImageMapper.toProductImageResponse(productImageRepository.save(newImage));
    }
}
