package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.request.ProductSearchRequest;
import com.e_commerce.e_commerce.entity.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> findBySearchCriteria(ProductSearchRequest request);
}
