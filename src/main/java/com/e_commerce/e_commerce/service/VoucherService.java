package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.VoucherCreationRequest;
import com.e_commerce.e_commerce.dto.request.VoucherUpdateRequest;
import com.e_commerce.e_commerce.dto.response.VoucherResponse;
import com.e_commerce.e_commerce.entity.Voucher;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.VoucherMapper;
import com.e_commerce.e_commerce.repository.VoucherRepository;
import com.e_commerce.e_commerce.util.ParseUtils;
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
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VoucherService {
    VoucherMapper voucherMapper;
    VoucherRepository voucherRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public VoucherResponse createVoucher(VoucherCreationRequest request) {
        Voucher voucher = voucherMapper.toVoucher(request);
        voucher.setCategory(ParseUtils.parseCategory(request.getCategory()));
        return voucherMapper.toVoucherResponse(voucherRepository.save(voucher));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public VoucherResponse getVoucherByCode(String voucherCode) {
        Voucher voucher = voucherRepository.findByCode(voucherCode).orElseThrow(() -> new AppException(ErrorCode.VOUCHER_CODE_NOT_EXISTED));
        return voucherMapper.toVoucherResponse(voucher);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<VoucherResponse> getAllVouchers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return voucherRepository.findAll(pageable).map(voucher -> voucherMapper.toVoucherResponse(voucher));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public VoucherResponse updateVoucher(String voucherCode, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findByCode(voucherCode).orElseThrow(() -> new AppException(ErrorCode.VOUCHER_CODE_NOT_EXISTED));
        if (request.getDiscountAmount() != null && request.getDiscountPercent() != null) {
            throw new AppException(ErrorCode.VOUCHER_DISCOUNT_BOTH_PRESENT);
        }
        voucherMapper.updateVoucher(voucher, request);
        voucher.setDiscountAmount(request.getDiscountAmount());
        voucher.setDiscountPercent(request.getDiscountPercent());
        if (request.getCategory() != null) {
            voucher.setCategory(ParseUtils.parseCategory(request.getCategory()));
        }
        return voucherMapper.toVoucherResponse(voucherRepository.save(voucher));
    }
}
