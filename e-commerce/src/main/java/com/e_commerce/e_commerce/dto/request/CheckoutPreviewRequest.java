package com.e_commerce.e_commerce.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutPreviewRequest {
    List<String> cartItemIds;
    // key is cart item id, value is voucher code
    Map<String, String> cartItemIdToVoucherCodeMap;
    Integer service_id;
    String to_ward_code;
    Integer to_district_id;
    String shippingVoucherCode;
}
