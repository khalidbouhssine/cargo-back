package com.car.cargo.repository;


import com.car.cargo.models.Client;
import com.car.cargo.models.Reservation;
import com.car.cargo.models.Voiture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    List<Reservation> findByIdClient(Client client);
    
 // Trouver des réservations qui se chevauchent avec la période donnée (excluant une réservation par son ID)
 // Trouver des réservations qui se chevauchent avec la période donnée (excluant une réservation par son ID)
    // Trouver des réservations qui chevauchent avec la période donnée, excluant une réservation par son ID
    @Query("SELECT r FROM Reservation r WHERE r.voiture.idVoiture = :voitureId " +
           "AND (r.startDate BETWEEN :startDate AND :endDate OR r.endDate BETWEEN :startDate AND :endDate) " +
           "AND r.idReservation != :excludedReservationId")
    List<Reservation> findByVoitureIdVoitureAndStartDateBetween(@Param("voitureId") Long voitureId,
                                                                @Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate,
                                                                @Param("excludedReservationId") Long excludedReservationId);

 
    
    
}