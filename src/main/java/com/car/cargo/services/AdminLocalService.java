package com.car.cargo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.car.cargo.models.AdminLocal;
import com.car.cargo.models.VerificationCodeAdminLocal;
import com.car.cargo.repository.AdminLocalRepository;
import com.car.cargo.repository.VerificationCodeAdminLocalRepository;

@Service
public class AdminLocalService {

    @Autowired
    private AdminLocalRepository adminLocalRepository;
    
    @Autowired
    private VerificationCodeAdminLocalRepository verificationCodeAdminLocalRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Ajouter un nouvel AdminLocal
    public AdminLocal addAdminLocal(AdminLocal adminLocal) {
        return adminLocalRepository.save(adminLocal);
    }

    // Trouver un AdminLocal par email
    public AdminLocal findByEmail(String email) {
        return adminLocalRepository.findByEmail(email);
    }

    // Mettre à jour un AdminLocal
    public AdminLocal updateAdminLocal(AdminLocal adminLocal) {
        return adminLocalRepository.save(adminLocal);
    }
    
    public void sendVerificationCode(AdminLocal adminLocal) {
        // Generate a 6-digit random code
        String code = String.format("%06d", (int) (Math.random() * 1_000_000));

        // Create and save the VerificationCodeAdminLocal entity
        VerificationCodeAdminLocal verificationCode = new VerificationCodeAdminLocal();
        verificationCode.setCode(code);
        verificationCode.setAdminLocal(adminLocal);
        verificationCodeAdminLocalRepository.save(verificationCode);

        // Send the code via email
        emailService.sendEmail(
            adminLocal.getEmail(),
            "Your Verification Code",
            "Your verification code is: " + code
        );
    }
    
    public boolean verifyCode(AdminLocal adminLocal, String code) {
        // Find the most recent verification code for the admin
        VerificationCodeAdminLocal latestCode = verificationCodeAdminLocalRepository
                .findTopByAdminLocalOrderByCreatedAtDesc(adminLocal);

        if (latestCode == null || !latestCode.getCode().equals(code)) {
            return false; // Code does not match
        }

        return true; // Code matches
    }
    
    public void changePassword(AdminLocal adminLocal, String newPassword) {
        // Encrypt the new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        adminLocal.setPassword(encodedPassword);

        // Save the updated admin
        adminLocalRepository.save(adminLocal);
    }
    public Map<String, Object> getAllAdminLocalsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminLocal> adminLocalsPage = adminLocalRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("adminsLocaux", adminLocalsPage.getContent());
        result.put("currentPage", adminLocalsPage.getNumber());
        result.put("totalItems", adminLocalsPage.getTotalElements());
        result.put("totalPages", adminLocalsPage.getTotalPages());

        return result;
    }


    public AdminLocal findById(Long id) {
        Optional<AdminLocal> adminLocal = adminLocalRepository.findById(id);
        return adminLocal.orElse(null); // Retourne null si l'AdminLocal n'existe pas
    }
    
 // Supprimer un AdminLocal par ID
    public void deleteAdminLocalById(Long id) {
        if (adminLocalRepository.existsById(id)) {
            adminLocalRepository.deleteById(id); // Supprime si l'ID existe
        } else {
            throw new IllegalArgumentException("AdminLocal avec l'ID " + id + " n'existe pas."); // Lève une exception si l'ID est introuvable
        }
    }


}
