package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.VoucherCreationRequest;
import com.e_commerce.e_commerce.dto.request.VoucherUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.VoucherResponse;
import com.e_commerce.e_commerce.service.VoucherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VoucherController {
    VoucherService voucherService;

    @PostMapping
    ApiResponse<VoucherResponse> createVoucher(@Valid @RequestBody VoucherCreationRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.createVoucher(request))
                .build();
    }

    @GetMapping("/{voucherCode}")
    ApiResponse<VoucherResponse> getVoucherByCode(@PathVariable String voucherCode) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.getVoucherByCode(voucherCode))
                .build();
    }

    @GetMapping
    ApiResponse<Page<VoucherResponse>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.<Page<VoucherResponse>>builder()
                .result(voucherService.getAllVouchers(page, size, sortBy, sortDir))
                .build();
    }

    @PutMapping("/{voucherCode}")
    ApiResponse<VoucherResponse> updateVoucher(
            @PathVariable String voucherCode,
            @Valid @RequestBody VoucherUpdateRequest request
    ) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.updateVoucher(voucherCode, request))
                .build();
    }
}
