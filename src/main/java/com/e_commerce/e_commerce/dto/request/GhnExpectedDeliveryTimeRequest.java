package com.e_commerce.e_commerce.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnExpectedDeliveryTimeRequest {
    Integer from_district_id;
    Integer to_district_id;
    String from_ward_code;
    String to_ward_code;
    Integer service_id;
}
