package com.car.cargo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.car.cargo.models.AdminGlobal;
import com.car.cargo.repository.AdminGlobalRepository;

@Service
public class AdminGlobalService {
	@Autowired
    private AdminGlobalRepository adminGlobalRepository;
	public AdminGlobal addAdminGlobal(AdminGlobal adminGlobal) {
        return adminGlobalRepository.save(adminGlobal); // Enregistre le client dans la base de données
    }
	 public AdminGlobal findByEmail(String email) {
	        return adminGlobalRepository.findByEmail(email);
	    }
}
