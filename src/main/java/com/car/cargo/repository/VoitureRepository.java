package com.car.cargo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.car.cargo.models.Voiture;

@Repository
public interface VoitureRepository extends JpaRepository<Voiture, Long> {
    boolean existsByLicenceplate(String licencePlate);
    
    @Query(value = """
            SELECT v.*
            FROM Voiture v
            WHERE v.status = 'Disponible'
            AND v.id_voiture IN (
                SELECT r.id_voiture
                FROM Reservation r
                WHERE 
                    (r.start_date NOT BETWEEN :startDate AND :endDate)
                    AND (r.end_date NOT BETWEEN :startDate AND :endDate)
                    AND (:startDate NOT BETWEEN r.start_date AND r.end_date)
                    AND (:endDate NOT BETWEEN r.start_date AND r.end_date)
                    AND r.end_city = :cityId
            )
            """, nativeQuery = true)
    List<Voiture> findAvailableCars(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("cityId") Long cityId);
}
