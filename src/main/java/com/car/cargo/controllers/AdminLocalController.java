package com.car.cargo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.car.cargo.models.AdminLocal;
import com.car.cargo.services.AdminLocalService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/adminLocal")
@CrossOrigin(origins = "*")
public class AdminLocalController {

    @Autowired
    private AdminLocalService adminLocalService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String SECRET_KEY = "VFAbCGus7Mr0laauDiYfHsNgkUHXfgaok10ior2lYwxsuetda/uf4l4QYzfGtAyxylRFGpkzfMR44Vey0qGcUg==";

    // Créer un compte AdminLocal
    @PostMapping("/createAdminLocalAccount")
    public ResponseEntity<AdminLocal> createAdminLocal(@RequestBody AdminLocal adminLocal) {
        if (adminLocal.getNomComplet() == null || adminLocal.getEmail() == null || 
            adminLocal.getPassword() == null || adminLocal.getCity() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        adminLocal.setPassword(passwordEncoder.encode(adminLocal.getPassword()));
        adminLocal.setImageProfile(null);

        AdminLocal savedAdminLocal = adminLocalService.addAdminLocal(adminLocal);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAdminLocal);
    }

    // Connexion d'un AdminLocal
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        AdminLocal adminLocal = adminLocalService.findByEmail(email);
        if (adminLocal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email"));
        }

        if (!passwordEncoder.matches(password, adminLocal.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
        }

        String token = Jwts.builder()
                .setSubject(adminLocal.getEmail())
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes())
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // Afficher le profil d'un AdminLocal
    @GetMapping("/profile")
    public ResponseEntity<?> getAdminLocalProfile(@RequestHeader("Authorization") String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            String email = claims.getSubject();
            AdminLocal adminLocal = adminLocalService.findByEmail(email);
            if (adminLocal == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "AdminLocal not found"));
            }

            return ResponseEntity.ok(adminLocal);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        }
    }
    
    @PostMapping("/sendCode")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email is required"));
        }

        AdminLocal adminLocal = adminLocalService.findByEmail(email);
        if (adminLocal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "AdminLocal not found"));
        }

        adminLocalService.sendVerificationCode(adminLocal);
        return ResponseEntity.ok(Map.of("message", "Verification code sent successfully"));
    }
    
    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String code = requestBody.get("code");

        if (email == null || code == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email and code are required"));
        }

        // Find the admin by email
        AdminLocal adminLocal = adminLocalService.findByEmail(email);
        if (adminLocal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "AdminLocal not found"));
        }

        // Verify the code
        boolean isCodeValid = adminLocalService.verifyCode(adminLocal, code);
        if (!isCodeValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid code"));
        }

        return ResponseEntity.ok(Map.of("message", "Code verified successfully"));
    }
    
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String newPassword = requestBody.get("newPassword");

        // Validate inputs
        if (email == null || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email and new password are required"));
        }

        // Find the admin by email
        AdminLocal adminLocal = adminLocalService.findByEmail(email);
        if (adminLocal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "AdminLocal not found"));
        }

        // Change the password
        adminLocalService.changePassword(adminLocal, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    
}
