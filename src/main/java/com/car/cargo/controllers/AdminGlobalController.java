package com.car.cargo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.car.cargo.models.AdminGlobal;
import com.car.cargo.models.City;
import com.car.cargo.models.Client;
import com.car.cargo.models.VerificationCode;
import com.car.cargo.models.VerificationCodeAdminG;
import com.car.cargo.repository.VerificationCodeRepository;
import com.car.cargo.repository.VerificationCodeRepositoryG;
import com.car.cargo.services.AdminGlobalService;
import com.car.cargo.services.CityService;
import com.car.cargo.services.EmailService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/adminGlobal")
@CrossOrigin(origins = "*")
public class AdminGlobalController {
	@Autowired
    private AdminGlobalService adminGlobalService;
	 @Autowired
	 private BCryptPasswordEncoder passwordEncoder;
	 @Autowired
	 private VerificationCodeRepositoryG verificationCodeRepositoryG;
	 @Autowired
	 private EmailService emailService;
	 @Autowired
	 private CityService cityService;
	 
	
	 private static final String SECRET_KEY = "VFAbCGus7Mr0laauDiYfHsNgkUHXfgaok10ior2lYwxsuetda/uf4l4QYzfGtAyxylRFGpkzfMR44Vey0qGcUg=="; 
	 
	 //create admin globale account
	 @PostMapping("/createAdminGlobalAccount")
	    public ResponseEntity<AdminGlobal> createUser(@RequestBody AdminGlobal adminGlobal) {
	        // Vérifiez et définissez uniquement les champs nécessaires
	        if (adminGlobal.getNomComplet() == null || adminGlobal.getEmail() == null || 
	        		adminGlobal.getPassword() == null || adminGlobal.getCity() == null) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	        }
	        
	        adminGlobal.setPassword(passwordEncoder.encode(adminGlobal.getPassword()));

	        // Laissez les autres champs nuls si non spécifiés
	        adminGlobal.setImageProfile(null);

	        AdminGlobal savedAdminGlobal = adminGlobalService.addAdminGlobal(adminGlobal);
	        return ResponseEntity.status(HttpStatus.CREATED).body(savedAdminGlobal);
	    }
	    //login by admin global 
	    @PostMapping("/login")
	    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
	        String email = loginRequest.get("email");
	        String password = loginRequest.get("password");

	        // Rechercher le client par email
	        AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
	        if (adminGlobal == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email"));
	        }

	        if (!passwordEncoder.matches(password, adminGlobal.getPassword())) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
	        }

	        // Générer un JWT avec le CIN récupéré de la base de données
	        String token = Jwts.builder()
	                .setSubject(adminGlobal.getEmail())
	                .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes())
	                .compact();

	        // Retourner le token
	        Map<String, String> response = new HashMap<>();
	        response.put("token", token);
	        return ResponseEntity.ok(response);
	    }
	  //afficher profile admin globale
	    @GetMapping("/profile")
	    public ResponseEntity<?> getClientProfile(@RequestHeader("Authorization") String token) {
	        try {
	            // Vérifier et décoder le token
	            Claims claims = Jwts.parser()
	                    .setSigningKey(SECRET_KEY.getBytes())
	                    .parseClaimsJws(token.replace("Bearer ", ""))
	                    .getBody();

	            // Récupérer l'email depuis le token
	            String email = claims.getSubject();

	            // Récupérer le client par email
	            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
	            if (adminGlobal == null) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "admin global not found"));
	            }

	            // Retourner les informations du client
	            return ResponseEntity.ok(adminGlobal);
	        } catch (JwtException | IllegalArgumentException e) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
	        }
	    }
	    
	    @PostMapping("/generateCode")
	    public ResponseEntity<?> generateVerificationCode(@RequestParam("email") String email) {
	        try {
	            // Find the client by email
	            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
	            if (adminGlobal == null) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Admin not found"));
	            }

	            // Generate a 6-digit random code
	            String code = String.format("%06d", (int) (Math.random() * 1_000_000));

	            // Create and save the VerificationCode entity
	            VerificationCodeAdminG verificationCodeAdminG = new VerificationCodeAdminG();
	            verificationCodeAdminG.setCode(code);
	            verificationCodeAdminG.setAdminGlobal(adminGlobal);
	            verificationCodeRepositoryG.save(verificationCodeAdminG);

	            // Send the code via email (mocked here, replace with actual email sending logic)
	            // Assuming you have an email service
	            emailService.sendEmail(
	                email,
	                "Your Verification Code",
	                "Your verification code is: " + code
	            );

	            return ResponseEntity.ok(Map.of("message", "Verification code generated and sent successfully"));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
	        }
	    }
	    
	    
	    @PostMapping("/verifyCode")
	    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> requestBody) {
	        try {
	            // Extract email and code from the request body
	            String email = requestBody.get("email");
	            String code = requestBody.get("code");

	            if (email == null || code == null) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email and code are required"));
	            }

	            // Find the admin by email
	            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
	            if (adminGlobal == null) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Admin not found"));
	            }

	            // Find the most recent verification code for the admin
	            VerificationCodeAdminG verificationCodeAdminG = verificationCodeRepositoryG.findTopByAdminGlobalOrderByCreatedAtDesc(adminGlobal);
	            if (verificationCodeAdminG == null || !verificationCodeAdminG.getCode().equals(code)) {
	                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid code"));
	            }

	            // If the code matches, return a success response
	            return ResponseEntity.ok(Map.of("message", "Code verified successfully"));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
	        }
	    }
	    
	    
	    @PostMapping("/changePassword")
	    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> requestBody) {
	        try {
	            // Extract email and new password from the request body
	            String email = requestBody.get("email");
	            String newPassword = requestBody.get("newPassword");

	            // Validate input
	            if (email == null || newPassword == null || newPassword.isEmpty()) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(Map.of("error", "Email and new password are required"));
	            }

	            // Find the admin by email
	            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
	            if (adminGlobal == null) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Admin not found"));
	            }

	            // Encrypt the new password
	            String encodedPassword = passwordEncoder.encode(newPassword);

	            // Update the password
	            adminGlobal.setPassword(encodedPassword);
	            adminGlobalService.addAdminGlobal(adminGlobal); // Save the updated admin

	            // Return success response
	            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", e.getMessage()));
	        }
	    }
	    
	 // Ajoouter une ville depuis adminGlobal en verifiant son identite 
	    @PostMapping("/addCityWithValidation")
	    public ResponseEntity<?> addCityWithValidation(
	            @RequestBody Map<String, String> requestBody,
	            @RequestHeader("Authorization") String token) {
	        try {
	            // Extract nameCity from the request body
	            String nameCity = requestBody.get("nameCity");

	            if (nameCity == null || nameCity.trim().isEmpty()) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body(Map.of("error", "City name is required"));
	            }

	            // Validate and decode the token to extract admin's email
	            Claims claims = Jwts.parser()
	                    .setSigningKey(SECRET_KEY.getBytes())
	                    .parseClaimsJws(token.replace("Bearer ", ""))
	                    .getBody();

	            String email = claims.getSubject();

	            // Check if the admin exists
	            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
	            if (adminGlobal == null) {
	                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                        .body(Map.of("error", "Invalid or expired token"));
	            }

	            // Call the CityController's create route
	            RestTemplate restTemplate = new RestTemplate();
	            String cityControllerUrl = "http://localhost:8080/city/create"; // Replace with your actual CityController URL

	            // Prepare request body
	            Map<String, String> cityRequest = new HashMap<>();
	            cityRequest.put("nameCity", nameCity);

	            ResponseEntity<?> cityResponse = restTemplate.postForEntity(cityControllerUrl, cityRequest, Object.class);

	            // Return the response from the CityController
	            return cityResponse;

	        } catch (JwtException | IllegalArgumentException e) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Map.of("error", "Invalid or expired token"));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("error", e.getMessage()));
	        }
	    }
	 // delete a city by admin globle en verfiant son identite
	    @DeleteMapping("/deleteCityWithValidation/{cityId}")
	    public ResponseEntity<?> deleteCityWithValidation(
	            @PathVariable("cityId") Long cityId,
	            @RequestHeader("Authorization") String token) {
	        try {
	            // Validate and decode the token to extract admin's email
	            Claims claims = Jwts.parser()
	                    .setSigningKey(SECRET_KEY.getBytes())
	                    .parseClaimsJws(token.replace("Bearer ", ""))
	                    .getBody();

	            String email = claims.getSubject();

	            // Check if the admin exists
	            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
	            if (adminGlobal == null) {
	                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                        .body(Map.of("error", "Invalid or expired token"));
	            }

	            // Call the CityController's delete route
	            RestTemplate restTemplate = new RestTemplate();
	            String cityControllerUrl = "http://localhost:8080/city/delete/" + cityId; // Replace with your actual CityController URL

	            // Send DELETE request
	            restTemplate.delete(cityControllerUrl);

	            // Return success response
	            return ResponseEntity.ok(Map.of("message", "City deleted successfully"));

	        } catch (JwtException | IllegalArgumentException e) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Map.of("error", "Invalid or expired token"));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(Map.of("error", e.getMessage()));
	        }
	    }



	   
}
