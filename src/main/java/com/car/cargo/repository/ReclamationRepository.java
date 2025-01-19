package com.car.cargo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.car.cargo.models.Reclamation;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    // Vous pouvez ajouter des méthodes personnalisées si nécessaire, par exemple :
    // List<Reclamation> findByClientId(Long clientId);
}
