package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.request.ProductSearchRequest;
import com.e_commerce.e_commerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<Product> findBySearchCriteria(ProductSearchRequest request, boolean isAdmin, Pageable pageable);
}
