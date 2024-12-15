package com.car.cargo.controllers;

import com.car.cargo.models.Payement;
import com.car.cargo.services.PayementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payement")
public class PayementController {

    @Autowired
    private PayementService payementService;

    // Route to add a payment
    @PostMapping("/add")
    public ResponseEntity<?> addPayement(@RequestBody Payement payement) {
        try {
            // Call the service to add the payment
            Payement newPayement = payementService.addPayement(payement);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPayement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred while adding the payment."));
        }
    }
    @GetMapping("/afficher/{id}")
    public ResponseEntity<?> getPayementById(@PathVariable Long id) {
        try {
            Payement payement = payementService.getPayementById(id);
            if (payement != null) {
                return ResponseEntity.ok(payement);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Payment not found."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred while retrieving the payment."));
        }
    }
    @GetMapping("/afficher/all")
    public ResponseEntity<?> getAllPayements() {
        try {
            Iterable<Payement> payements = payementService.getAllPayements();
            return ResponseEntity.ok(payements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred while retrieving the payments."));
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePayement(@PathVariable Long id) {
        try {
            boolean isDeleted = payementService.deletePayement(id);
            if (isDeleted) {
                return ResponseEntity.ok(Map.of("message", "Payment deleted successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Payment not found."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred while deleting the payment."));
        }
    }


}

