package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.ProductCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductResponse;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.enums.Category;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.ProductMapper;
import com.e_commerce.e_commerce.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
        product.setCategory(Category.valueOf(productCreationRequest.getCategory().toUpperCase()));
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public ProductResponse getProduct(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return productMapper.toProductResponse(product);
    }

    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream().map((product) -> productMapper.toProductResponse(product)).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(String productId, ProductUpdateRequest productUpdateRequest) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        productMapper.updateProduct(product, productUpdateRequest);
        if (productUpdateRequest.getCategory() != null) {
            product.setCategory(Category.valueOf(productUpdateRequest.getCategory().toUpperCase()));
        }
        return productMapper.toProductResponse(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(String productId) {
        productRepository.deleteById(productId);
    }
}
