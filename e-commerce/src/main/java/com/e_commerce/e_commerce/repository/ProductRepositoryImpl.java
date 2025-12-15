package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.request.ProductSearchRequest;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.entity.QProduct;
import com.e_commerce.e_commerce.entity.QProductVariant;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Product> findBySearchCriteria(ProductSearchRequest request) {
        QProduct product = QProduct.product;
        QProductVariant productVariant = QProductVariant.productVariant;

        BooleanBuilder where = new BooleanBuilder();

        if (hasText(request.getKeyword())) {
            where.and(product.name.containsIgnoreCase(request.getKeyword()));
        }

        if (notEmpty(request.getBrands())) {
            where.and(product.brand.in(request.getBrands()));
        }

        if (notEmpty(request.getRams())) {
            where.and(productVariant.ram.in(request.getRams()));
        }

        if (notEmpty(request.getStorages())) {
            where.and(productVariant.storage.in(request.getStorages()));
        }

        if (notEmpty(request.getCpus())) {
            where.and(productVariant.cpu.in(request.getCpus()));
        }

        if (notEmpty(request.getGpus())) {
            where.and(productVariant.gpu.in(request.getGpus()));
        }

        if (notEmpty(request.getScreenSizes())) {
            where.and(productVariant.screenSize.in(request.getScreenSizes()));
        }

        if (notEmpty(request.getScreenResolutions())) {
            where.and(productVariant.screenResolution.in(request.getScreenResolutions()));
        }

        if (notEmpty(request.getRefreshRatesHz())) {
            where.and(productVariant.refreshRateHz.in(request.getRefreshRatesHz()));
        }

        if (request.getMinPrice() != null) {
            where.and(productVariant.price.goe(request.getMinPrice()));
        }

        if (request.getMaxPrice() != null) {
            where.and(productVariant.price.loe(request.getMaxPrice()));
        }

        return jpaQueryFactory
                .selectDistinct(product)
                .from(product)
                .join(product.productVariants, productVariant)
                .where(where)
                .fetch();
    }

    private boolean notEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
