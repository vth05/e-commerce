package com.e_commerce.e_commerce.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnOrderInfoResponse {
    Integer shop_id;
    String return_name;
    String return_phone;
    String return_address;
    String return_ward_code;
    Integer return_district_id;
    String from_name;
    String from_phone;
    String from_address;
    String from_ward_code;
    Integer from_district_id;
    String to_name;
    String to_phone;
    String to_address;
    String to_ward_code;
    Integer to_district_id;
    Integer service_type_id;
    Integer service_id;
    String required_note;
    String order_code;
    Instant leadtime;
    Instant order_date;
    String status;
}
