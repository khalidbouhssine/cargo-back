package com.car.cargo.controllers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.car.cargo.models.AdminGlobal;
import com.car.cargo.models.AdminLocal;
import com.car.cargo.models.Voiture;
import com.car.cargo.repository.AdminLocalRepository;
import com.car.cargo.services.AdminGlobalService;
import com.car.cargo.services.VoitureService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/voiture")
@CrossOrigin(origins = "*")
public class VoitureController {
    @Autowired
    private VoitureService voitureService;
    @Autowired
    private AdminGlobalService adminGlobalRepository;
    @Autowired
    private AdminLocalRepository adminLocalRepository;
    @Autowired
    private AdminGlobalService adminGlobalService;
    private static final String SECRET_KEY = "VFAbCGus7Mr0laauDiYfHsNgkUHXfgaok10ior2lYwxsuetda/uf4l4QYzfGtAyxylRFGpkzfMR44Vey0qGcUg==";
    @PostMapping("/add")
    public ResponseEntity<?> addVoiture(@RequestHeader("Authorization") String token, 
                                         @RequestParam("brand") String brand,
                                         @RequestParam("model") String model,
                                         @RequestParam("licenceplate") String licenceplate,
                                         @RequestParam("status") String status,
                                         @RequestParam("pricePerDay") double pricePerDay,
                                         @RequestParam("kolometrage") double kolometrage,
                                         @RequestParam("dateFabrication") String dateFabrication, // Nouvelle entrée pour la date
                                         @RequestParam("image") MultipartFile image) {
        try {
            // Vérifier et décoder le token
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            // Obtenir l'email depuis le token
            String email = claims.getSubject();

            // Vérifier si l'email appartient à un admin
            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);
            if (adminGlobal == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized: Admin role required"));
            }

            // Création de la nouvelle voiture
            Voiture voiture = new Voiture();
            voiture.setBrand(brand);
            voiture.setModel(model);
            voiture.setLicenceplate(licenceplate);
            voiture.setStatus(status);
            voiture.setPricePerDay(pricePerDay);
            voiture.setKolometrage(kolometrage);
            
            // Convertir la dateFabrication reçue en String en LocalDateTime
            voiture.setDateFabrication(LocalDateTime.parse(dateFabrication)); // Parse la date en LocalDateTime

            // Gestion du téléchargement de l'image
            if (image != null && !image.isEmpty()) {
                System.out.println("Processing image upload...");

                // Création d'un RestTemplate pour appeler l'API de téléchargement d'image
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", token); // Ajouter le token d'autorisation

                // Préparer la requête multipart
                MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
                bodyBuilder.part("image", image.getResource());

                HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(bodyBuilder.build(), headers);
                String uploadUrl = "http://localhost:8081/api/upload"; // URL de votre service de téléchargement

                // Appeler l'API de téléchargement d'image
                ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to upload image"));
                }

                // Extraire l'ID de l'image depuis la réponse
                Map<String, Object> responseBody = response.getBody();
                String imageId = responseBody != null ? (String) responseBody.get("message") : null;

                if (imageId == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Invalid response from upload service"));
                }

                // Définir l'ID de l'image dans l'objet Voiture
                voiture.setImagevoiture(Long.parseLong(imageId));
                System.out.println("Image uploaded successfully with ID: " + imageId);
            } else {
                System.out.println("No image provided.");
            }

            // Sauvegarder la voiture
            Voiture newVoiture = voitureService.addVoiture(voiture);
            System.out.println("Voiture added successfully with ID: " + newVoiture.getIdVoiture());

            return ResponseEntity.status(HttpStatus.CREATED).body(newVoiture);
        } catch (IllegalArgumentException e) {
            System.out.println("Illegal argument exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("An error occurred while adding the car: " + e.getMessage());
            e.printStackTrace(); // Afficher la trace de l'exception pour le débogage
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while adding the car.", "error", e.getMessage()));
        }
    }


 //  route pour supprimer une voiture par ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteVoiture(@PathVariable Long id) {
        try {
            boolean isDeleted = voitureService.deleteVoiture(id);
            if (isDeleted) {
                return ResponseEntity.ok(Map.of("message", "Voiture supprimée avec succès."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Voiture introuvable."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Une erreur s'est produite lors de la suppression de la voiture."));
        }
    }
 // Route pour afficher une voiture par ID
    @GetMapping("/affichervoiture/{id}")
    public ResponseEntity<?> getVoitureById(@PathVariable Long id) {
        try {
            Voiture voiture = voitureService.getVoitureById(id);
            if (voiture != null) {
                return ResponseEntity.ok(voiture);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Voiture introuvable."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Une erreur s'est produite lors de la récupération de la voiture."));
        }
    }
   
    @GetMapping("/all")
    public ResponseEntity<?> getAllVoitures(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size) {
        try {
            // Vérifier si le token est présent et commence par "Bearer "
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token manquant ou invalide."));
            }

            // Vérifier et décoder le token
            Claims claims;
            try {
                claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY.getBytes()) // Utiliser votre clé secrète
                        .parseClaimsJws(token.replace("Bearer ", ""))
                        .getBody();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token invalide."));
            }

            // Récupérer l'email depuis le token
            String email = claims.getSubject();

            // Vérifier si l'email appartient à un admin global ou local
            AdminGlobal adminGlobal = adminGlobalRepository.findByEmail(email);
            AdminLocal adminLocal = adminLocalRepository.findByEmail(email);

            if (adminGlobal == null && adminLocal == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Accès refusé : utilisateur non autorisé."));
            }

            // Si l'utilisateur est valide, récupérer les voitures avec pagination
            System.out.println("----------------" + page + "---------------");
            Map<String, Object> result = voitureService.getAllVoituresWithPagination(page, size);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Une erreur s'est produite lors de la récupération des voitures."));
        }
    }



}
