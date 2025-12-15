package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.ProductCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductSearchRequest;
import com.e_commerce.e_commerce.dto.request.ProductUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductResponse;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.ProductMapper;
import com.e_commerce.e_commerce.repository.ProductRepository;
import com.e_commerce.e_commerce.repository.ProductRepositoryImpl;
import com.e_commerce.e_commerce.util.ParseUtils;
import com.e_commerce.e_commerce.util.ProductUtils;
import com.e_commerce.e_commerce.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ProductService {
    ProductMapper productMapper;
    ProductRepository productRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(ProductCreationRequest productCreationRequest) {
        Product product = productMapper.toProduct(productCreationRequest);
        product.setCategory(ParseUtils.parseCategory(productCreationRequest.getCategory()));
        product.setCode(ProductUtils.generateProductCode());
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public ProductResponse getProduct(String productId) {
        boolean isAdmin = SecurityUtils.isAdmin();
        Product product;
        if (isAdmin) {
            product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        } else {
            product = productRepository.findByIdAndActiveTrue(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        }
        return productMapper.toProductResponse(product);
    }

    public Page<ProductResponse> getProducts(int page, int size, String sortBy, String sortDir) {
        boolean isAdmin = SecurityUtils.isAdmin();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products;
        if (isAdmin) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findAllByActiveTrue(pageable);
        }

        return products.map(product -> productMapper.toProductResponse(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(String productId, ProductUpdateRequest productUpdateRequest) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        productMapper.updateProduct(product, productUpdateRequest);
        if (productUpdateRequest.getCategory() != null) {
            product.setCategory(ParseUtils.parseCategory(productUpdateRequest.getCategory()));
        }
        return productMapper.toProductResponse(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse deactivateProduct(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        product.setActive(false);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> findBySearchCriteria(ProductSearchRequest request) {
        return productRepository.findBySearchCriteria(request).stream().map(product -> productMapper.toProductResponse(product)).toList();
    }
}
