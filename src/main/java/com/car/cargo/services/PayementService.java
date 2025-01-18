package com.car.cargo.services;
import com.car.cargo.models.Payement;
import com.car.cargo.models.Reservation;
import com.car.cargo.repository.PayementRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayementService {

    @Autowired
    private PayementRepository payementRepository;

    public Payement addPayement(Payement payement) {
        // Validate payment method
        if (!payement.getPayementMethod().equalsIgnoreCase("Visa") &&
            !payement.getPayementMethod().equalsIgnoreCase("MasterCard")) {
            throw new IllegalArgumentException("The payment method must be either Visa or MasterCard.");
        }

        // Save the payment to the database
        return payementRepository.save(payement);
    }
    public Payement getPayementById(Long id) {
        return payementRepository.findById(id).orElse(null);
    }
    public Iterable<Payement> getAllPayements() {
        return payementRepository.findAll();
    }
    public boolean deletePayement(Long id) {
        try {
            if (payementRepository.existsById(id)) {
                payementRepository.deleteById(id);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }



    // Méthode pour récupérer le paiement associé à une réservation
    public Payement findByReservation(Reservation reservation) {
        return payementRepository.findByReservation(reservation);
    }
}

