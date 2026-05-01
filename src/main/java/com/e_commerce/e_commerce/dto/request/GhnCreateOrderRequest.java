package com.e_commerce.e_commerce.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GhnCreateOrderRequest {
    String from_name;
    String to_name;
    String from_phone;
    String to_phone;
    String from_address;
    String to_address;
    String from_ward_name;
    String from_district_name;
    String from_province_name;
    String to_ward_code;
    Integer to_district_id;
    Integer weight;
    Integer length;
    Integer width;
    Integer height;
    Integer service_type_id;
    Integer payment_type_id;
    String required_note;
    List<GhnOrderItem> items;
}
