package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCodeAndActiveTrue(String voucherCode);

    Optional<Voucher> findByCode(String voucherCode);

    List<Voucher> findAllByCodeIn(Collection<String> voucherCodes);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.code IN :codes")
    List<Voucher> findAllByCodeInWithWriteLock(@Param("codes") Collection<String> codes);
}
