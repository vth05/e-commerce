package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {
    @NotBlank(message = "RECEIVER_NAME_REQUIRED")
    String receiverName;

    @NotBlank(message = "RECEIVER_PHONE_REQUIRED")
    String receiverPhone;

    @NotBlank(message = "SHIPPING_ADDRESS_REQUIRED")
    String shippingAddress;

    @NotBlank(message = "PAYMENT_METHOD_REQUIRED")
    String paymentMethod;

    List<String> cartItemIds;

    // key is cart item id, value is voucher code
    Map<String, String> cartItemIdToVoucherCodeMap;

    Integer service_id;

    String to_ward_code;

    Integer to_district_id;

    String shippingVoucherCode;

    String required_note;
}
