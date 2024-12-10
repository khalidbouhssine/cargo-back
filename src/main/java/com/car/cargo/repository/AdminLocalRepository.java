package com.car.cargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.car.cargo.models.AdminLocal;

public interface AdminLocalRepository extends JpaRepository<AdminLocal, Long> {
    AdminLocal findByEmail(String email);
}
