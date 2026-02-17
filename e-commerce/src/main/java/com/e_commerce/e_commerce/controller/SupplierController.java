package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.SupplierCreationRequest;
import com.e_commerce.e_commerce.dto.request.SupplierUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.SupplierResponse;
import com.e_commerce.e_commerce.service.SupplierService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SupplierController {
    SupplierService supplierService;

    @PostMapping
    ApiResponse<SupplierResponse> createSupplier(@RequestBody @Valid SupplierCreationRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.createSupplier(request))
                .build();
    }

    @GetMapping
    ApiResponse<Page<SupplierResponse>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.<Page<SupplierResponse>>builder()
                .result(supplierService.getSuppliers(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/{supplierId}")
    ApiResponse<SupplierResponse> getSupplierById(@PathVariable String supplierId) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.getSupplierById(supplierId))
                .build();
    }

    @PutMapping("/{supplierId}")
    ApiResponse<SupplierResponse> updateSupplier(@PathVariable String supplierId, @RequestBody @Valid SupplierUpdateRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.updateSupplier(supplierId, request))
                .build();
    }

    @DeleteMapping("/{supplierId}")
    ApiResponse<SupplierResponse> deactivateSupplier(@PathVariable String supplierId) {
        return ApiResponse.<SupplierResponse>builder()
                .message("Supplier deactivated successfully")
                .result(supplierService.deactivateSupplier(supplierId))
                .build();
    }
}
