package com.car.cargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.car.cargo.models.Voiture;

public interface VoitureRepository extends JpaRepository<Voiture, Long> {
    boolean existsByLicenceplate(String licencePlate);
}
