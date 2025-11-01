package com.e_commerce.e_commerce.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    KEY_INVALID(8888, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1001, "Username at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1002, "Password at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "This user does not already exist", HttpStatus.NOT_FOUND),
    USERNAME_REQUIRED(1004, "Username is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1005, "Password is required", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1006, "Email is required", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1007, "Email is invalid", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS(1008, "This user already exists", HttpStatus.CONFLICT),
    UNAUTHENTICATED(1009, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    SECURITY(1010, "Security", HttpStatus.UNAUTHORIZED),
    TOKEN_REQUIRED(1011, "Token is required", HttpStatus.BAD_REQUEST),
    ROLE_NAME_REQUIRED(1012, "Role name is required", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_REQUIRED(1013, "Permission name is required", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1014, "Unauthorized", HttpStatus.FORBIDDEN),
    PRODUCT_NOT_EXISTED(1015, "This product does not already exist", HttpStatus.NOT_FOUND),
    PRODUCT_NAME_REQUIRED(1016, "Product name is required", HttpStatus.BAD_REQUEST),
    PRODUCT_CATEGORY_REQUIRED(1017, "Product category is required", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_REQUIRED(1018, "Product price is required", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_INVALID(1019, "Product price must be at least 1", HttpStatus.BAD_REQUEST),
    PRODUCT_INSUFFICIENT_STOCK(1020, "Product stock is insufficient", HttpStatus.BAD_REQUEST),
    PRODUCT_ID_REQUIRED(1021, "Product ID is required", HttpStatus.BAD_REQUEST),
    PRODUCT_QUANTITY_INVALID(1022, "Product quantity must be at least 1", HttpStatus.BAD_REQUEST),
    PRODUCT_QUANTITY_REQUIRED(1023, "Product quantity is required", HttpStatus.BAD_REQUEST),
    PRODUCT_NAME_INVALID(1024, "Product name at least {min} characters", HttpStatus.BAD_REQUEST),
    PRODUCT_CATEGORY_INVALID(1025, "Product category at least {min} characters", HttpStatus.BAD_REQUEST),
    CART_ID_REQUIRED(1026, "Cart ID is required", HttpStatus.BAD_REQUEST),
    PRICE_AT_PURCHASE_REQUIRED(1027, "Price at purchase is required", HttpStatus.BAD_REQUEST),
    PRICE_AT_PURCHASE_INVALID(1028, "Price at purchase must be at least 1", HttpStatus.BAD_REQUEST),
    CART_NOT_EXISTED(1029, "This cart does not already exist", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_EXISTED(1030, "This cart item does not already exist", HttpStatus.NOT_FOUND),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;
}
