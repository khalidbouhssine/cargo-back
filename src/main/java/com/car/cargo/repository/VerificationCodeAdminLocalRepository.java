package com.car.cargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.car.cargo.models.AdminLocal;
import com.car.cargo.models.VerificationCodeAdminLocal;

public interface VerificationCodeAdminLocalRepository extends JpaRepository<VerificationCodeAdminLocal, Long> {
    VerificationCodeAdminLocal findTopByAdminLocalOrderByCreatedAtDesc(AdminLocal adminLocal);
}

