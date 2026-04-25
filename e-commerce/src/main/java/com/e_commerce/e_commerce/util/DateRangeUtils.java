package com.e_commerce.e_commerce.util;

import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;

import java.time.LocalDate;
import java.time.LocalTime;

public class DateRangeUtils {
    public static DateRange normalizeDateRange(LocalDate start, LocalDate end) {
        if (start == null) start = LocalDate.now().minusDays(30);
        if (end == null) end = LocalDate.now();
        if (start.isAfter(end)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        return new DateRange(start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }
}
