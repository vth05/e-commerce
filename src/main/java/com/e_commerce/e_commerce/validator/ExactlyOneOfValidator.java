package com.e_commerce.e_commerce.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class ExactlyOneOfValidator implements ConstraintValidator<ExactlyOneOf, Object> {
    private String[] fields;

    @Override
    public void initialize(ExactlyOneOf constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        fields = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object == null) {
            return true;
        }

        int count = 0;
        for (String fieldName : fields) {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    count++;
                }
            } catch (Exception ignored) {
                return false;
            }
        }

        return count == 1;
    }
}
