package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.ProductVariantCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductVariantUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductVariantResponse;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.entity.ProductVariant;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.ProductVariantMapper;
import com.e_commerce.e_commerce.repository.ProductRepository;
import com.e_commerce.e_commerce.repository.ProductVariantRepository;
import com.e_commerce.e_commerce.util.ProductVariantUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ProductVariantService {
    ProductVariantRepository productVariantRepository;
    ProductVariantMapper productVariantMapper;
    ProductRepository productRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponse createProductVariant(ProductVariantCreationRequest request, String productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        if (!product.isActive()) {
            throw new AppException(ErrorCode.PRODUCT_IS_NOT_ACTIVE);
        }
        ProductVariant productVariant = productVariantMapper.toProductVariant(request);
        productVariant.setProduct(product);
        productVariant.setProductVariantName(ProductVariantUtils.generateProductVariantName(productVariant.getProduct().getName(), productVariant.getColor(), productVariant.getRam(), productVariant.getStorage()));
        productVariant.setSku(ProductVariantUtils.generateSku(productVariant.getProduct().getName(), productVariant.getColor(), productVariant.getRam(), productVariant.getStorage()));
        try {
            productVariant = productVariantRepository.save(productVariant);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.SKU_ALREADY_EXISTED);
        }
        return productVariantMapper.toProductVariantResponse(productVariant);
    }

    public ProductVariantResponse getProductVariantById(String productVariantId) {
        boolean isAdmin = isAdmin();
        ProductVariant productVariant;
        if (isAdmin) {
            productVariant = productVariantRepository.findById(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        } else {
            productVariant = productVariantRepository.findByIdAndActiveTrue(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
            log.info("productVariant: {}", productVariant.isActive());
            productRepository.findByIdAndActiveTrue(productVariant.getProduct().getId()).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        }
        return productVariantMapper.toProductVariantResponse(productVariant);
    }

    public List<ProductVariantResponse> getProductVariantsByProductId(String productId) {
        boolean isAdmin = isAdmin();
        if (isAdmin) {
            return productVariantRepository.findAllByProductId(productId).stream().map(productVariant -> productVariantMapper.toProductVariantResponse(productVariant)).toList();
        }
        productRepository.findByIdAndActiveTrue(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        log.info("pass findByIdAndActiveTrue in productRepository");
        return productVariantRepository.findAllByProductIdAndActiveTrue(productId).stream().map(productVariant -> productVariantMapper.toProductVariantResponse(productVariant)).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponse updateProductVariant(ProductVariantUpdateRequest request, String productVariantId) {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        productVariantMapper.updateProductVariant(productVariant, request);
        productVariant.setProductVariantName(ProductVariantUtils.generateProductVariantName(productVariant.getProduct().getName(), productVariant.getColor(), productVariant.getRam(), productVariant.getStorage()));
        productVariant.setSku(ProductVariantUtils.generateSku(productVariant.getProduct().getName(), productVariant.getColor(), productVariant.getRam(), productVariant.getStorage()));
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

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}
