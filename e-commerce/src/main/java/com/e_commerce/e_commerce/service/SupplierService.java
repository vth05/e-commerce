package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.SupplierCreationRequest;
import com.e_commerce.e_commerce.dto.request.SupplierUpdateRequest;
import com.e_commerce.e_commerce.dto.response.SupplierResponse;
import com.e_commerce.e_commerce.entity.Supplier;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.SupplierMapper;
import com.e_commerce.e_commerce.repository.SupplierRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SupplierService {
    SupplierRepository supplierRepository;
    SupplierMapper supplierMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse createSupplier(SupplierCreationRequest request) {
        validateEmailAndPhoneUniqueForCreation(request.getEmail(), request.getPhone());
        return supplierMapper.toSupplierResponse(supplierRepository.save(supplierMapper.toSupplier(request)));
    }

    public SupplierResponse getSupplierById(String supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));
        return supplierMapper.toSupplierResponse(supplier);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<SupplierResponse> getSuppliers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Supplier> suppliers = supplierRepository.findAll(pageable);
        return suppliers.map(supplier -> supplierMapper.toSupplierResponse(supplier));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse updateSupplier(String supplierId, SupplierUpdateRequest request) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));
        validateEmailAndPhoneUniqueForUpdate(request.getEmail(), request.getPhone(), supplierId);
        supplierMapper.updateSupplier(supplier, request);
        return supplierMapper.toSupplierResponse(supplierRepository.save(supplier));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse deactivateSupplier(String supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));
        supplier.setActive(false);
        return supplierMapper.toSupplierResponse(supplierRepository.save(supplier));
    }

    private void validateEmailAndPhoneUniqueForCreation(String email, String phone) {
        if (email != null && supplierRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.SUPPLIER_EMAIL_ALREADY_EXISTS);
        }
        if (phone != null && supplierRepository.existsByPhone(phone)) {
            throw new AppException(ErrorCode.SUPPLIER_PHONE_ALREADY_EXISTS);
        }
    }

    private void validateEmailAndPhoneUniqueForUpdate(String email, String phone, String id) {
        if (email != null && supplierRepository.existsByEmailAndIdNot(email, id)) {
            throw new AppException(ErrorCode.SUPPLIER_EMAIL_ALREADY_EXISTS);
        }
        if (phone != null && supplierRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new AppException(ErrorCode.SUPPLIER_PHONE_ALREADY_EXISTS);
        }
    }
}
