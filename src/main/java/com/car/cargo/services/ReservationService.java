package com.car.cargo.services;

import com.car.cargo.models.Client;
import com.car.cargo.models.Reservation;
import com.car.cargo.repository.CityRepository;
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
    private final CityRepository cityRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository,VoitureRepository voitureRepository,CityRepository cityRepository) {
        this.reservationRepository = reservationRepository;
        this.voitureRepository = voitureRepository;
        this.cityRepository=cityRepository;
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

    public List<Reservation> findByClient(Client client) {
        return reservationRepository.findByIdClient(client);
    }
    
    ///////////////////////////////modifier une reservation/////////////////////////////////////////
 // Vérifier la disponibilité de la voiture pendant la période donnée, excluant l'ID de la réservation en cours
 // Vérifier la disponibilité d'une voiture pendant une période, excluant une réservation par son ID
    public boolean isCarAvailable(Long voitureId, LocalDateTime startDate, LocalDateTime endDate, Long excludedReservationId) {
        // Vérifier s'il existe une réservation qui chevauche la période demandée
        List<Reservation> conflictingReservations = reservationRepository.findByVoitureIdVoitureAndStartDateBetween(voitureId, startDate, endDate, excludedReservationId);
        return conflictingReservations.isEmpty();
    }

///////////////////////////////////////
 // Modifier une réservation
    public Reservation modifyReservation(Long idReservation, Long idVoiture, LocalDateTime startDate, LocalDateTime endDate, Long endCityId) {
        // Récupérer la réservation existante par son ID
        Optional<Reservation> existingReservationOpt = reservationRepository.findById(idReservation);
        if (existingReservationOpt.isEmpty()) {
            return null;  // Ou vous pouvez lancer une exception si vous préférez
        }

        Reservation existingReservation = existingReservationOpt.get();

        // Mettre à jour les informations de la réservation avec les nouvelles valeurs
        existingReservation.setVoiture(voitureRepository.findById(idVoiture).orElse(null));
        existingReservation.setStartDate(startDate);
        existingReservation.setEndDate(endDate);
        existingReservation.setEndCity(cityRepository.findById(endCityId).orElse(null));

        // Sauvegarder et retourner la réservation mise à jour
        return reservationRepository.save(existingReservation);
    }
     
    public Reservation findById(Long idReservation) {
        Optional<Reservation> existingReservationOpt = reservationRepository.findById(idReservation);
        if (existingReservationOpt.isPresent()) {
            return existingReservationOpt.get();
        } else {
            return null; // Ou vous pouvez lancer une exception si vous préférez
        }
    }


    
}