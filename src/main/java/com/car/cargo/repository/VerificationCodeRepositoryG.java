package com.car.cargo.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.car.cargo.models.AdminGlobal;
import com.car.cargo.models.VerificationCodeAdminG;


public interface VerificationCodeRepositoryG extends JpaRepository<VerificationCodeAdminG, Long> {
	VerificationCodeAdminG findTopByAdminGlobalOrderByCreatedAtDesc(AdminGlobal adminGlobal);
}
