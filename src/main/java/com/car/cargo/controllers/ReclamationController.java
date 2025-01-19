package com.car.cargo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.car.cargo.models.Client;
import com.car.cargo.models.Reclamation;
import com.car.cargo.services.ClientService;
import com.car.cargo.services.ReclamationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/reclamations")
public class ReclamationController {

	@Autowired
    private  ReclamationService reclamationService;
	
	@Autowired
    private  ClientService clientService;
	 private static final String SECRET_KEY = "VFAbCGus7Mr0laauDiYfHsNgkUHXfgaok10ior2lYwxsuetda/uf4l4QYzfGtAyxylRFGpkzfMR44Vey0qGcUg==";
	@PostMapping("/addReclamation")
	public ResponseEntity<?> addReclamation(@RequestBody Map<String, String> requestBody, 
	                                        @RequestHeader("Authorization") String token) {
	    try {
	        // Vérifier et décoder le token
	        Claims claims = Jwts.parser()
	                .setSigningKey(SECRET_KEY.getBytes())
	                .parseClaimsJws(token.replace("Bearer ", ""))
	                .getBody();

	        // Récupérer l'email du client depuis le token
	        String email = claims.getSubject();
	        if (email == null || email.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token does not contain valid email"));
	        }

	        // Récupérer le client par email
	        Client client = clientService.findByEmail(email);
	        if (client == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found"));
	        }

	        // Extraire les informations de la réclamation depuis le corps de la requête
	        String objetReclamation = requestBody.get("objetReclamation");
	        String message = requestBody.get("message");
	        String telephone = requestBody.get("telephone");

	        if (objetReclamation == null || message == null || telephone == null) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing required fields"));
	        }

	        // Créer une nouvelle réclamation
	        Reclamation reclamation = new Reclamation();
	        reclamation.setObjetReclamation(objetReclamation);
	        reclamation.setMessage(message);
	        reclamation.setTelephone(telephone);
	        reclamation.setClient(client);

	        // Sauvegarder la réclamation dans la base de données
	        Reclamation createdReclamation = reclamationService.addReclamation(reclamation);

	        // Retourner la réclamation créée
	        return ResponseEntity.status(HttpStatus.CREATED).body(createdReclamation);

	    } catch (JwtException | IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
	    }
	}

    
    

}