package com.e_commerce.e_commerce.util;

import com.e_commerce.e_commerce.dto.request.GhnCalculateFeeRequest;
import com.e_commerce.e_commerce.dto.response.ShippingFeeCalculationResult;
import com.e_commerce.e_commerce.entity.CartItem;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.entity.ProductVariant;
import com.e_commerce.e_commerce.entity.Voucher;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.VoucherRepository;
import com.e_commerce.e_commerce.service.GhnService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CheckoutUtils {
    GhnService ghnService;
    VoucherRepository voucherRepository;

    public BigDecimal safe(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    public void validateVoucher(Voucher voucher) {
        if (voucher.getUsageCount() >= voucher.getUsageLimit()) {
            throw new AppException(ErrorCode.VOUCHER_OUT_OF);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getValidFrom())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_ACTIVE);
        }
        if (now.isAfter(voucher.getValidTo())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED);
        }
    }

    public boolean isVoucherApplicableToProduct(Voucher voucher, Product product) {
        if (voucher.getCategory() != product.getCategory()) {
            return false;
        }
        validateVoucher(voucher);
        return true;
    }

    public BigDecimal calculateCartItemDiscount(Voucher voucher, BigDecimal cartItemSubtotal) {
        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountAmount() != null && cartItemSubtotal.compareTo(voucher.getMinOrderValue()) >= 0) {
            discount = BigDecimal.valueOf(voucher.getDiscountAmount());
            if (discount.compareTo(cartItemSubtotal) > 0) {
                discount = cartItemSubtotal;
            }
        } else if (voucher.getDiscountPercent() != null && cartItemSubtotal.compareTo(voucher.getMinOrderValue()) >= 0) {
            discount = cartItemSubtotal.multiply(BigDecimal.valueOf(voucher.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            if (discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discount = voucher.getMaxDiscountAmount();
            }
        }
        return discount;
    }

    public ShippingFeeCalculationResult calculateShippingFeeAndDimensions(BigDecimal subtotal, Integer serviceId, String toWardCode, Integer toDistrictId, List<CartItem> cartItemsFromRequest) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal maxLength = BigDecimal.ZERO;
        BigDecimal maxWidth = BigDecimal.ZERO;
        BigDecimal totalHeight = BigDecimal.ZERO;
        BigDecimal insuranceValue = subtotal;
        for (CartItem cartItem : cartItemsFromRequest) {
            ProductVariant productVariant = cartItem.getProductVariant();
            totalWeight = totalWeight.add(safe(productVariant.getWeight()).multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            List<BigDecimal> dims = Arrays.asList(
                    safe(productVariant.getLength()),
                    safe(productVariant.getWidth()),
                    safe(productVariant.getHeight())
            );
            Collections.sort(dims, Collections.reverseOrder());
            maxLength = maxLength.max(dims.get(0));
            maxWidth = maxWidth.max(dims.get(1));
            totalHeight = totalHeight.add(dims.get(2).multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        int weightInGrams = totalWeight.setScale(0, RoundingMode.CEILING).intValueExact();
        int lengthInCm = maxLength.setScale(0, RoundingMode.CEILING).intValueExact();
        int widthInCm = maxWidth.setScale(0, RoundingMode.CEILING).intValueExact();
        int heightInCm = totalHeight.setScale(0, RoundingMode.CEILING).intValueExact();
        GhnCalculateFeeRequest ghnCalculateFeeRequest = GhnCalculateFeeRequest.builder()
                .service_id(serviceId)
                .from_ward_code("21906")
                .to_ward_code(toWardCode)
                .from_district_id(1458)
                .to_district_id(toDistrictId)
                .weight(weightInGrams)
                .length(lengthInCm)
                .width(widthInCm)
                .height(heightInCm)
                .insurance_value(insuranceValue.setScale(0, RoundingMode.CEILING).intValueExact())
                .build();
        return ShippingFeeCalculationResult.builder()
                .originalShippingFee(BigDecimal.valueOf(ghnService.calculateFee(ghnCalculateFeeRequest).getTotal()))
                .weightInGrams(weightInGrams)
                .lengthInCm(lengthInCm)
                .widthInCm(widthInCm)
                .heightInCm(heightInCm)
                .build();
    }

    public BigDecimal calculateShippingDiscount(BigDecimal subtotal, BigDecimal originalShippingFee, String shippingVoucherCode) {
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        if (shippingVoucherCode != null) {
            Voucher shippingVoucher = voucherRepository.findByCode(shippingVoucherCode).orElseThrow(() -> new AppException(ErrorCode.VOUCHER_CODE_NOT_EXISTED));
            validateVoucher(shippingVoucher);
            if (shippingVoucher.getDiscountAmount() != null && subtotal.compareTo(shippingVoucher.getMinOrderValue()) >= 0) {
                shippingDiscount = BigDecimal.valueOf(shippingVoucher.getDiscountAmount());
            } else if (shippingVoucher.getDiscountPercent() != null && subtotal.compareTo(shippingVoucher.getMinOrderValue()) >= 0) {
                shippingDiscount = originalShippingFee.multiply(BigDecimal.valueOf(shippingVoucher.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                if (shippingDiscount.compareTo(shippingVoucher.getMaxDiscountAmount()) > 0) {
                    shippingDiscount = shippingVoucher.getMaxDiscountAmount();
                }
            }
            if (shippingDiscount.compareTo(originalShippingFee) > 0) {
                shippingDiscount = originalShippingFee;
            }
        }
        return shippingDiscount;
    }
}
