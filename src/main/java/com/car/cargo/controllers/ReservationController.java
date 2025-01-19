package com.car.cargo.controllers;

import com.car.cargo.models.AdminGlobal;
import com.car.cargo.models.AdminLocal;
import com.car.cargo.models.City;
import com.car.cargo.models.Client;
import com.car.cargo.models.Payement;
import com.car.cargo.models.Reservation;
import com.car.cargo.models.Voiture;
import com.car.cargo.repository.AdminGlobalRepository;
import com.car.cargo.repository.ClientRepository;
import com.car.cargo.repository.PayementRepository;
import com.car.cargo.repository.ReservationRepository;
import com.car.cargo.services.AdminGlobalService;
import com.car.cargo.services.AdminLocalService;
import com.car.cargo.services.CityService;
import com.car.cargo.services.ClientService;
import com.car.cargo.services.EmailService;
import com.car.cargo.services.PayementService;
import com.car.cargo.services.ReservationService;
import com.car.cargo.services.VoitureService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
    
    @Autowired
    private AdminLocalService adminLocalService;

    @Autowired
    private AdminGlobalService adminGlobalService;
     @Autowired
     private PayementService payementService;
     
     @Autowired
     private CityService cityService;
     
     @Autowired
     private VoitureService voitureService;
     
     @Autowired
     private PayementRepository payementRepository;
     
     @Autowired
     private AdminGlobalRepository adminGlobalRepository;
     
     @Autowired 
     private ClientRepository clientRepository;
     
     @Autowired 
     private ReservationRepository reservationRepository;
     
     
     @Autowired
     private EmailService emailService;
     
   

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

            // Récupérer le client, adminLocal et adminGlobal par email
            Client client = clientService.findByEmail(email);
            AdminLocal adminLocal = adminLocalService.findByEmail(email);
            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);

            if (client == null && adminLocal == null && adminGlobal == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client, AdminLocal, or AdminGlobal not found"));
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
    
    
    
    
    
 // Cette route qui fait la réservation puis le paiement dans une seule route
    @PostMapping("/addReservationAndPayment")
    public ResponseEntity<?> addReservationAndPayment(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        try {
            // Récupérer le token depuis l'en-tête de la requête
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is missing or invalid"));
            }

            // Décoder le token JWT
            Claims claims = null;
            try {
                claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY.getBytes())
                        .parseClaimsJws(token.replace("Bearer ", ""))
                        .getBody();
            } catch (JwtException | IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
            }

            // Récupérer l'email depuis le token
            String email = claims.getSubject();
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token does not contain valid email"));
            }

            // Récupérer le client par email
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Extraire les données du corps de la requête
            Long idVoiture = Long.valueOf(requestBody.get("idVoiture").toString());
            Long startCityId = Long.valueOf(requestBody.get("startCity").toString());
            Long endCityId = Long.valueOf(requestBody.get("endCity").toString());
            String startDateStr = requestBody.get("startDate").toString();
            String endDateStr = requestBody.get("endDate").toString();
            double amount = Double.parseDouble(requestBody.get("amount").toString());
            String payementMethod = requestBody.get("payementMethod") != null ? requestBody.get("payementMethod").toString() : "Credit Card"; // Par défaut "Credit Card"

            // Convertir les dates de String à LocalDateTime
            LocalDateTime startDate = LocalDateTime.parse(startDateStr);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr);

            // Vérification des dates
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Start date must be before end date"));
            }

            // Récupérer les entités nécessaires
            Voiture voiture = voitureService.getVoitureById(idVoiture);
            if (voiture == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Car not found"));
            }

            City startCity = cityService.findById(startCityId);
            City endCity = cityService.findById(endCityId);
            if (startCity == null || endCity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Start or end city not found"));
            }

            // Créer une nouvelle réservation
            Reservation reservation = new Reservation();
            reservation.setVoiture(voiture);
            reservation.setStartCity(startCity);
            reservation.setEndCity(endCity);
            reservation.setStartDate(startDate);
            reservation.setEndDate(endDate);
            reservation.setStatus("Pending");
            reservation.setIdClient(client);

            // Sauvegarder la réservation
            Reservation createdReservation = reservationService.createReservation(reservation);

            // Créer un paiement lié à la réservation
            Payement payement = new Payement();
            payement.setReservation(createdReservation);
            payement.setAmount(amount);
            payement.setPayementMethod(payementMethod); // Utilisation de la valeur reçue
            payement.setPayementDate(LocalDateTime.now());

            // Sauvegarder le paiement
            Payement createdPayement = payementService.addPayement(payement);

            // Envoyer l'email au client après le paiement réussi
            String subject = "Your Reservation and Payment Confirmation";
            String body = String.format("Hello Mr. %s, your reservation has been successfully made for the %s (%s) car, " +
                            "registration number %s, from %s to %s, for an amount of %.2f DH, from the city of %s to %s.\n\nCarGo Foundation",
                    client.getNomComplet(), voiture.getBrand(), voiture.getModel(), voiture.getLicenceplate(),
                    startDate.toLocalDate(), endDate.toLocalDate(), amount, startCity.getNameCity(), endCity.getNameCity());

            // Envoyer l'email
            emailService.sendEmail(client.getEmail(), subject, body);

            // Retourner la réponse combinée
            Map<String, Object> response = new HashMap<>();
            response.put("reservation", createdReservation);
            response.put("payement", createdPayement);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
    
    @GetMapping("/myReservations")
    public ResponseEntity<?> getClientReservations(HttpServletRequest request) {
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

            // Récupérer les réservations du client
            List<Reservation> reservations = reservationService.findByClient(client);
            if (reservations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No reservations found for the client"));
            }

            // Créer une liste de Map personnalisée
            RestTemplate restTemplate = new RestTemplate();
            List<Map<String, Object>> customReservations = reservations.stream().map(reservation -> {
                Map<String, Object> reservationMap = new HashMap<>();
                reservationMap.put("idReservation", reservation.getIdReservation());

                // Ajouter les informations sur la voiture
                Voiture voiture = reservation.getVoiture();
                Map<String, Object> voitureMap = new HashMap<>();
                voitureMap.put("idVoiture", voiture.getIdVoiture());
                voitureMap.put("brand", voiture.getBrand());
                voitureMap.put("model", voiture.getModel());
                voitureMap.put("licenceplate", voiture.getLicenceplate());
                voitureMap.put("pricePerDay", voiture.getPricePerDay());

                // Vérification et récupération du nom de l'image
                if (voiture.getImagevoiture() != null) {
                    Long imageId = voiture.getImagevoiture();
                    String imageUrl = "http://localhost:8081/api/image/" + imageId;

                    try {
                        ResponseEntity<Map> response = restTemplate.getForEntity(imageUrl, Map.class);
                        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                            String imageName = (String) response.getBody().get("message");
                            voitureMap.put("imageName", imageName);
                        } else {
                            voitureMap.put("imageName", "Image not found");
                        }
                    } catch (Exception e) {
                        voitureMap.put("imageName", "Error fetching image");
                    }
                } else {
                    voitureMap.put("imageName", "No image ID provided");
                }

                reservationMap.put("voiture", voitureMap);

                // Ajouter les informations des villes de départ et d'arrivée (uniquement le nom)
                Map<String, Object> startCityMap = Map.of("nameCity", reservation.getStartCity().getNameCity());
                Map<String, Object> endCityMap = Map.of("nameCity", reservation.getEndCity().getNameCity());

                reservationMap.put("startCity", startCityMap);
                reservationMap.put("endCity", endCityMap);

                // Ajouter les dates de début et de fin, ainsi que le statut
                reservationMap.put("startDate", reservation.getStartDate());
                reservationMap.put("endDate", reservation.getEndDate());
                reservationMap.put("status", reservation.getStatus());

                // Récupérer le paiement associé à la réservation
                Payement payement = payementService.findByReservation(reservation);
                if (payement != null) {
                    reservationMap.put("amount", payement.getAmount());
                } else {
                    reservationMap.put("amount", "No payment found");
                }

                return reservationMap;
            }).toList();

            // Retourner les réservations personnalisées
            return ResponseEntity.ok(customReservations);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
    //////////////////////////////////////////modifcation de reservation selon la disponibilitte /////////////////////////////////
      
    @PutMapping("/updateReservation")
    public ResponseEntity<?> updateReservation(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        try {
            // Récupérer le token depuis l'en-tête de la requête
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token is missing or invalid"));
            }

            // Décoder le token JWT
            Claims claims = null;
            try {
                claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY.getBytes())
                        .parseClaimsJws(token.replace("Bearer ", ""))
                        .getBody();
            } catch (JwtException | IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Récupérer l'email depuis le token
            String email = claims.getSubject();
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token does not contain valid email"));
            }

            // Récupérer le client par email
            Client client = clientService.findByEmail(email);
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Client not found"));
            }

            // Extraire l'ID de réservation du body
            Long idReservation = requestBody.containsKey("idReservation") 
                    ? Long.valueOf(requestBody.get("idReservation").toString()) 
                    : null;
            if (idReservation == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Reservation ID is required"));
            }

            // Rechercher la réservation existante
            Reservation existingReservation = reservationService.findById(idReservation);
            if (existingReservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Reservation not found"));
            }

            // Vérifier si la réservation appartient au client
            if (!existingReservation.getIdClient().getEmail().equals(client.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not allowed to modify this reservation"));
            }

            // Extraire les informations envoyées dans la requête pour la mise à jour
            Long idVoiture = requestBody.containsKey("idVoiture") 
                    ? Long.valueOf(requestBody.get("idVoiture").toString()) 
                    : null;
            String startDateStr = requestBody.containsKey("startDate") 
                    ? requestBody.get("startDate").toString() 
                    : null;
            String endDateStr = requestBody.containsKey("endDate") 
                    ? requestBody.get("endDate").toString() 
                    : null;
            Long endCityId = requestBody.containsKey("endCity") 
                    ? Long.valueOf(requestBody.get("endCity").toString()) 
                    : null;
            Double amount = requestBody.containsKey("amount") 
                    ? Double.valueOf(requestBody.get("amount").toString()) 
                    : null;

            // Vérification si l'attribut amount est fourni
            if (amount == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Amount is required"));
            }

            // Convertir les dates de String à LocalDateTime
            LocalDateTime startDate = (startDateStr != null) 
                    ? LocalDateTime.parse(startDateStr) 
                    : existingReservation.getStartDate();
            LocalDateTime endDate = (endDateStr != null) 
                    ? LocalDateTime.parse(endDateStr) 
                    : existingReservation.getEndDate();

            // Vérification des dates
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Start date must be before end date"));
            }

            // Récupérer la voiture si l'ID de la voiture a été fourni
            Voiture voiture = (idVoiture != null) 
                    ? voitureService.getVoitureById(idVoiture) 
                    : existingReservation.getVoiture();
            if (voiture == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Car not found"));
            }

            // Récupérer la ville de destination si elle a été fournie
            City endCity = (endCityId != null) 
                    ? cityService.findById(endCityId) 
                    : existingReservation.getEndCity();
            if (endCity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "End city not found"));
            }

            // Vérification de la disponibilité de la voiture dans la nouvelle période
            boolean isAvailable = reservationService.isCarAvailable(
                    voiture.getIdVoiture(), startDate, endDate, idReservation);
            if (!isAvailable) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "The car is not available during the selected dates"));
            }

            // Mettre à jour la réservation avec les nouveaux détails
            Reservation updatedReservation = reservationService.modifyReservation(
                    idReservation, idVoiture, startDate, endDate, endCityId);

            // Enregistrer ou mettre à jour le paiement associé
            Payement existingPayement = payementRepository.findByReservation(updatedReservation);
            if (existingPayement == null) {
                existingPayement = new Payement();
                existingPayement.setReservation(updatedReservation);
            }
            existingPayement.setAmount(amount);
            payementRepository.save(existingPayement);

            // Retourner la réponse
            return ResponseEntity.ok(Map.of(
                    "updatedReservation", updatedReservation,
                    "newPayment", existingPayement
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
    /////////////////////////////recupere toutes les reservation exsistent par admin Gloabal //////
   
    @GetMapping("/allReservations")
    public ResponseEntity<?> getAllReservations(
            @RequestHeader("Authorization") String token,
            @RequestParam int page,
            @RequestParam int size) {
        try {
            // Vérifier et décoder le token
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            // Récupérer l'email depuis le token
            String email = claims.getSubject();

            // Vérifier si l'email appartient à un admin global
            AdminGlobal adminGlobal = adminGlobalRepository.findByEmail(email);
            if (adminGlobal == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied, not an admin"));
            }

            // Récupérer les réservations avec pagination
            Map<String, Object> result = reservationService.getReservationsWithPagination(page, size);

            return ResponseEntity.ok(result); // Retourner la réponse paginée des réservations

        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred while fetching reservations"));
        }
    }

////////////////////////////route pour faire la supression d'une reservation et son payement selon id 
    @DeleteMapping("/deleteReservation/{id}")
    public ResponseEntity<?> deleteReservation(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // Vérifier et décoder le token
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            // Récupérer l'email depuis le token
            String email = claims.getSubject();

            // Vérifier si l'email appartient à un admin global ou si c'est le client associé à la réservation
            AdminGlobal adminGlobal = adminGlobalRepository.findByEmail(email);
            Client client = clientRepository.findByEmail(email);

            // Récupérer la réservation par ID
            Reservation reservation = reservationRepository.findById(id).orElse(null);
            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservation not found"));
            }

            // Si l'utilisateur est un admin global ou le client propriétaire de la réservation
            if (adminGlobal == null && (client == null || !reservation.getIdClient().getIdClient().equals(client.getIdClient()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
            }

            // Supprimer le paiement associé à la réservation
            Payement payement = payementService.findByReservation(reservation);
            if (payement != null) {
                payementService.delete(payement); // Assurez-vous que cette méthode supprime le paiement de la base de données
            }

            // Supprimer la réservation
            reservationRepository.delete(reservation);

            return ResponseEntity.ok(Map.of("message", "Reservation and associated payment deleted successfully"));

        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }


    
    ///////////////////////////////////////////////////////////////////////////////
    @GetMapping("/reservationDetails/{id}")
    public ResponseEntity<?> getReservationDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // Vérifier et décoder le token
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            // Récupérer l'email depuis le token
            String email = claims.getSubject();

            // Vérifier si l'email appartient à un admin global
            AdminGlobal adminGlobal = adminGlobalRepository.findByEmail(email);
            if (adminGlobal == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied, not an admin global"));
            }

            // Récupérer la réservation par ID
            Reservation reservation = reservationRepository.findById(id).orElse(null);
            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservation not found"));
            }

            // Récupérer le client associé à la réservation
            Client client = reservation.getIdClient();
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
            }

            // Récupérer les villes de départ et d'arrivée
            City startCity = reservation.getStartCity();
            City endCity = reservation.getEndCity();

            String startCityName = startCity.getNameCity() ;
            String endCityName =  endCity.getNameCity() ;

            // Créer une Map contenant les informations demandées
            Map<String, Object> reservationDetails = new HashMap<>();
            reservationDetails.put("idReservation", reservation.getIdReservation());
            reservationDetails.put("idClient", client.getIdClient());
            reservationDetails.put("nomComplet", client.getNomComplet());  // Ajout du nom complet du client
            reservationDetails.put("startCity", startCityName);  // Nom de la ville de départ
            reservationDetails.put("endCity", endCityName);  // Nom de la ville d'arrivée
            reservationDetails.put("startDate", reservation.getStartDate());
            reservationDetails.put("endDate", reservation.getEndDate());

            // Retourner les informations de la réservation
            return ResponseEntity.ok(reservationDetails);

        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }


    /////////////////////////////////////////////Reservation confirmation route////////////////////////////////////
    @PutMapping("/confirmReservation/{idReservation}")
    public ResponseEntity<?> confirmReservation(
            @PathVariable Long idReservation,
            @RequestHeader("Authorization") String token) {
        try {
            // Décoder le token JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            // Vérifier si l'utilisateur est un admin global
            String email = claims.getSubject();
            AdminGlobal adminGlobal = adminGlobalRepository.findByEmail(email);
            if (adminGlobal == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied, not an admin global"));
            }

            // Récupérer la réservation par ID
            Reservation reservation = reservationService.findById(idReservation);
            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reservation not found"));
            }

            // Vérifier si la réservation est déjà confirmée
            if ("Confirmed".equalsIgnoreCase(reservation.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Reservation is already confirmed"));
            }

            // Mettre à jour le statut de la réservation
            reservation.setStatus("Confirmed");
            reservationService.updateReservation(reservation);

            // Récupérer les informations du client
            Client client = reservation.getIdClient();
            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client associated with this reservation not found"));
            }

            // Envoyer un email au client
            String subject = "Your Reservation Confirmation";
            String body = String.format("Hello Mr. %s, your reservation for the car %s (%s) with registration number %s " +
                            "has been confirmed. The reservation is scheduled from %s to %s, from %s to %s.\n\nCarGo Foundation",
                    client.getNomComplet(),
                    reservation.getVoiture().getBrand(),
                    reservation.getVoiture().getModel(),
                    reservation.getVoiture().getLicenceplate(),
                    reservation.getStartDate().toLocalDate(),
                    reservation.getEndDate().toLocalDate(),
                    reservation.getStartCity().getNameCity(),
                    reservation.getEndCity().getNameCity());

            emailService.sendEmail(client.getEmail(), subject, body);

            // Réponse réussie
            return ResponseEntity.ok(Map.of(
                    "message", "Reservation confirmed successfully",
                    "reservationId", reservation.getIdReservation()
            ));

        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }


}