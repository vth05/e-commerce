package com.e_commerce.e_commerce.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnCalculateFeeRequest {
    Integer service_id;
    String from_ward_code;
    String to_ward_code;
    Integer from_district_id;
    Integer to_district_id;
    Integer weight;
    Integer length;
    Integer width;
    Integer height;
    Integer insurance_value;
    Integer cod_failed_amount;
    String coupon;
}
