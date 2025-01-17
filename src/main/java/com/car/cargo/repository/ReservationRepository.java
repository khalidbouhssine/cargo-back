package com.car.cargo.repository;


import com.car.cargo.models.Reservation;
import com.car.cargo.models.Voiture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Trouver une réservation par son ID
    Optional<Reservation> findById(Long idReservation);
    
    // Trouver des réservations par statut
    List<Reservation> findByStatus(String status);
    
    // Trouver des réservations par voiture (idVoiture)
    List<Reservation> findByVoitureIdVoiture(Long idVoiture);
    
    // Trouver des réservations entre deux dates
    List<Reservation> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    
    
}