package com.car.cargo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.MultiValueMap;

import com.car.cargo.models.Client;
import com.car.cargo.models.VerificationCode;
import com.car.cargo.repository.VerificationCodeRepository;
import com.car.cargo.services.ClientService;
import com.car.cargo.services.EmailService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    
    private static final String SECRET_KEY = "VFAbCGus7Mr0laauDiYfHsNgkUHXfgaok10ior2lYwxsuetda/uf4l4QYzfGtAyxylRFGpkzfMR44Vey0qGcUg=="; // Replace with your secret key

    //create client account
    @PostMapping("/registerClient")
    public ResponseEntity<Client> createUser(@RequestBody Client client) {
        // Vérifiez et définissez uniquement les champs nécessaires
        if (client.getNomComplet() == null || client.getEmail() == null || 
            client.getPassword() == null || client.getCity() == null || client.getCin() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        
        client.setPassword(passwordEncoder.encode(client.getPassword()));

        // Laissez les autres champs nuls si non spécifiés
        client.setAddresse(null);
        client.setImageProfile(null);

        Client savedClient = clientService.addClient(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
    }
    //route 
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        // Rechercher le client par email
        Client client = clientService.findByEmail(email);
        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email"));
        }

        // Récupérer le CIN depuis la base de données
        String cinFromDatabase = client.getCin();

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(password, client.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
        }

        // Générer un JWT avec le CIN récupéré de la base de données
        String token = Jwts.builder()
                .setSubject(client.getEmail())
                .claim("cin", cinFromDatabase)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes())
                .compact();

        // Retourner le token
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
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
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Retourner les informations du client
            return ResponseEntity.ok(client);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        }
    }
    
    @PostMapping("/uploadProfileImage")
    public ResponseEntity<?> uploadProfileImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("image") MultipartFile imageFile) {
        System.out.println("---------------------good-------------------");
        try {
            // Verify and decode the token
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            // Get the email from the token
            String email = claims.getSubject();

            // Retrieve the client by email
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Send the image to the external service
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Correct part name to 'image' as expected by the external API
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("image", imageFile.getResource()); // Use 'image' as the part name

            HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(bodyBuilder.build(), headers);
            String uploadUrl = "http://localhost:8081/api/upload";

            // Call the external API
            ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to upload image"));
            }

            // Extract the image ID from the response
            Map<String, Object> responseBody = response.getBody();
            String imageId = responseBody != null ? (String) responseBody.get("message") : null;

            if (imageId == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Invalid response from upload service"));
            }

            // Update the client's profile image ID
            client.setImageProfile(Long.parseLong(imageId));
            clientService.addClient(client);

            return ResponseEntity.ok(Map.of("message", "Profile image updated successfully", "imageId", imageId));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/generateCode")
    public ResponseEntity<?> generateVerificationCode(@RequestParam("email") String email) {
        try {
            // Find the client by email
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Generate a 6-digit random code
            String code = String.format("%06d", (int) (Math.random() * 1_000_000));

            // Create and save the VerificationCode entity
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setCode(code);
            verificationCode.setClient(client);
            verificationCodeRepository.save(verificationCode);

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
        String email = requestBody.get("email");
        String code = requestBody.get("code");

        if (email == null || code == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email and code are required"));
        }

        try {
            // Find the client by email
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Retrieve the last saved code for the client
            VerificationCode latestCode = verificationCodeRepository.findTopByClientOrderByCreatedAtDesc(client);
            if (latestCode == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No verification code found"));
            }

            // Check if the provided code matches
            if (latestCode.getCode().equals(code)) {
                return ResponseEntity.ok(Map.of("message", "Verification successful"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid code"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

 // update password route
    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String newPassword = requestBody.get("newPassword");

        if (email == null || newPassword == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email and newPassword are required"));
        }

        try {
            // Find the client by email
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Encrypt the new password
            String encodedPassword = passwordEncoder.encode(newPassword);

            // Update the password
            client.setPassword(encodedPassword);
            clientService.addClient(client); // Save the updated client

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    


}
