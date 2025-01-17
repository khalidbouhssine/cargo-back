package com.car.cargo.services;

import com.car.cargo.models.Reservation;
import com.car.cargo.repository.ReservationRepository;
import com.car.cargo.repository.VoitureRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VoitureRepository voitureRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository,VoitureRepository voitureRepository) {
        this.reservationRepository = reservationRepository;
        this.voitureRepository = voitureRepository;
    }

    // Créer une nouvelle réservation
    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    // Récupérer une réservation par son ID
    public Optional<Reservation> getReservationById(Long idReservation) {
        return reservationRepository.findById(idReservation);
    }

    // Récupérer toutes les réservations d'un certain statut
    public List<Reservation> getReservationsByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    // Récupérer les réservations pour une voiture spécifique
    public List<Reservation> getReservationsByCar(Long idVoiture) {
        return reservationRepository.findByVoitureIdVoiture(idVoiture);
    }

    // Récupérer les réservations entre deux dates
    public List<Reservation> getReservationsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findByStartDateBetween(startDate, endDate);
    }

    // Mettre à jour une réservation
    public Reservation updateReservation(Long idReservation, Reservation updatedReservation) {
        if (reservationRepository.existsById(idReservation)) {
            updatedReservation.setIdReservation(idReservation);
            return reservationRepository.save(updatedReservation);
        }
        return null;
    }

    // Supprimer une réservation
    public void deleteReservation(Long idReservation) {
        reservationRepository.deleteById(idReservation);
    }
    
    public List<Map<String, Object>> findAvailableCars(LocalDateTime startDate, LocalDateTime endDate, Long cityId) {
        // Requête pour trouver les voitures disponibles
        return voitureRepository.findAvailableCars(startDate, endDate, cityId)
                .stream()
                .map(voiture -> Map.<String, Object>of(
                    "idCar", voiture.getIdVoiture(),
                    "pricePerDay", voiture.getPricePerDay(),
                    "brand", voiture.getBrand(),
                    "imageVoiture", voiture.getImagevoiture()
                ))
                .toList();
    }

    
}