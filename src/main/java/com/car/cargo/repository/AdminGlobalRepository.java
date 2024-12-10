package com.car.cargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.car.cargo.models.AdminGlobal;

@Repository
public interface AdminGlobalRepository  extends JpaRepository<AdminGlobal, Long> {
	AdminGlobal findByEmail(String email);
}
