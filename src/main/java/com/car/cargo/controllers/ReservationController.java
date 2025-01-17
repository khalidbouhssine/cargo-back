package com.car.cargo.controllers;

import com.car.cargo.models.Client;
import com.car.cargo.models.Reservation;
import com.car.cargo.services.ClientService;
import com.car.cargo.services.ReservationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ClientService clientService;

    private static final String SECRET_KEY = "VFAbCGus7Mr0laauDiYfHsNgkUHXfgaok10ior2lYwxsuetda/uf4l4QYzfGtAyxylRFGpkzfMR44Vey0qGcUg==";

    @PostMapping("/addReservation")
    public ResponseEntity<?> addReservation(@RequestBody Reservation reservation, HttpServletRequest request) {
        try {
            // Récupérer le token depuis l'en-tête de la requête
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is missing or invalid"));
            }

            // Décoder le token JWT
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

            // Ajouter uniquement l'ID du client à la réservation
            reservation.setIdClient(client);

            // Sauvegarder la réservation
            Reservation createdReservation = reservationService.createReservation(reservation);

            // Retourner la réponse
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        }
    }
    
    @PostMapping("/available-cars")
    public ResponseEntity<?> getAvailableCars(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        try {
            // Récupérer le token depuis l'en-tête de la requête
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is missing or invalid"));
            }

            // Décoder le token JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            // Récupérer l'email depuis le token pour valider l'accès
            String email = claims.getSubject();
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Extraire les données du corps de la requête
            String startDateStr = (String) requestBody.get("startDate");
            String endDateStr = (String) requestBody.get("endDate");
            Long cityId = Long.valueOf(requestBody.get("cityId").toString());

            // Convertir les dates de String à LocalDateTime
            LocalDateTime startDate = LocalDateTime.parse(startDateStr);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr);

            // Vérification basique des dates
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Start date must be before end date"));
            }

            // Appeler le service pour obtenir les voitures disponibles
            var availableCars = reservationService.findAvailableCars(startDate, endDate, cityId);

            // Parcourir les voitures disponibles pour récupérer les noms d'images
            List<Map<String, Object>> mutableCarsList = new ArrayList<>();
            for (Map<String, Object> immutableCar : availableCars) {
                // Convertir la carte immuable en mutable
                Map<String, Object> car = new HashMap<>(immutableCar);

                if (car.containsKey("imageVoiture") && car.get("imageVoiture") != null) {
                    Long imageId = Long.valueOf(car.get("imageVoiture").toString());

                    // Construire l'URL de l'API distante
                    String imageUrl = "http://localhost:8081/api/image/" + imageId;
                    System.out.println("Fetching image from URL: " + imageUrl); // Log pour débogage

                    // Faire une requête HTTP GET pour récupérer le nom de l'image
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        ResponseEntity<Map> response = restTemplate.getForEntity(imageUrl, Map.class);

                        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                            String imageName = (String) response.getBody().get("message");
                            car.put("imageName", imageName); // Ajouter le nom de l'image au résultat
                        } else {
                            car.put("imageName", "Image not found");
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching image name for ID: " + imageId + " - " + e.getMessage());
                        car.put("imageName", "Error fetching image");
                    }
                } else {
                    car.put("imageName", "No image ID provided");
                }

                // Ajouter la carte mutable à la liste finale
                mutableCarsList.add(car);
            }

            // Retourner la liste des voitures disponibles avec les noms des images
            return ResponseEntity.ok(mutableCarsList);

        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        } catch (Exception e) {
            e.printStackTrace(); // Log l'exception complète
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }


}