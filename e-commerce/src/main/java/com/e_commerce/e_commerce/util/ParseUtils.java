package com.e_commerce.e_commerce.util;

import com.e_commerce.e_commerce.enums.Category;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.Gender;
import com.e_commerce.e_commerce.exception.AppException;

public class ParseUtils {
    public static Gender parseGender(String genderStr) {
        try {
            return Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_GENDER);
        }
    }

    public static Category parseCategory(String categoryStr) {
        try {
            return Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_CATEGORY);
        }
    }
}
