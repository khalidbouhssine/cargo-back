package com.car.cargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.car.cargo.models.Client;
import com.car.cargo.models.VerificationCode;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findTopByClientOrderByCreatedAtDesc(Client client);
}
