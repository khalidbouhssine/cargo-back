package com.car.cargo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.car.cargo.models.Reclamation;
import com.car.cargo.repository.ReclamationRepository;


@Service
public class ReclamationService {

	@Autowired
    private  ReclamationRepository reclamationRepository;

   

	    // Ajouter une nouvelle réclamation
	    public Reclamation addReclamation(Reclamation reclamation) {
	        // Sauvegarder la réclamation dans la base de données
	        return reclamationRepository.save(reclamation);
	    }
}