package com.car.cargo.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.car.cargo.models.Client;
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
	    
	    
	    public Map<String, Object> getReclamationsWithPagination(int page, int size) {
	        Pageable pageable = PageRequest.of(page, size);
	        Page<Reclamation> reclamationsPage = reclamationRepository.findAll(pageable);

	        // Créer une liste contenant uniquement les champs désirés pour les réclamations
	        List<Map<String, Object>> reclamationsList = reclamationsPage.getContent().stream()
	            .map(reclamation -> {
	                Map<String, Object> reclamationMap = new HashMap<>();
	                reclamationMap.put("idReclamation", reclamation.getIdReclamation());

	                // Ajouter directement le nom complet du client
	                Client client = reclamation.getClient();
	                reclamationMap.put("nomComplet", client.getNomComplet());  // Ajout du nom complet directement

	                // Ajouter la date de création de la réclamation
	                reclamationMap.put("dateReclamation", reclamation.getDateReclamation());

	                return reclamationMap;
	            })
	            .collect(Collectors.toList());

	        // Créer la réponse avec la pagination
	        Map<String, Object> result = new HashMap<>();
	        result.put("reclamations", reclamationsList);
	        result.put("totalElements", reclamationsPage.getTotalElements());
	        result.put("totalPages", reclamationsPage.getTotalPages());

	        return result;
	    }


}