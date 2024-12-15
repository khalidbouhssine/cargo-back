package com.car.cargo.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.car.cargo.models.Voiture;
import com.car.cargo.services.VoitureService;

@RestController
@RequestMapping("/voiture")
public class VoitureController {
    @Autowired
    private VoitureService voitureService;

    @PostMapping("/add")
    public ResponseEntity<?> addVoiture(@RequestBody Voiture voiture) {
        try {
            Voiture newVoiture = voitureService.addVoiture(voiture);
            return ResponseEntity.status(HttpStatus.CREATED).body(newVoiture);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding the car.");
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
    public ResponseEntity<?> getAllVoitures() {
        try {
            // Fetch all voitures from the service
            Iterable<Voiture> voitures = voitureService.getAllVoitures();
            return ResponseEntity.ok(voitures);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Une erreur s'est produite lors de la récupération des voitures."));
        }
    }


}
