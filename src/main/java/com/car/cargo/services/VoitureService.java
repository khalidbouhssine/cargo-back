package com.car.cargo.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.car.cargo.models.Voiture;
import com.car.cargo.repository.VoitureRepository;

@Service
public class VoitureService {
    @Autowired
    private VoitureRepository voitureRepository;

    public Voiture addVoiture(Voiture voiture) {
        // Vérifier si la plaque d'immatriculation existe déjà
        if (voitureRepository.existsByLicenceplate(voiture.getLicenceplate())) {
            throw new IllegalArgumentException("Licence plate already exists.");
        }

        // Valider le champ "status"
        if (!voiture.getStatus().equalsIgnoreCase("Disponible") &&
            !voiture.getStatus().equalsIgnoreCase("En cours") &&
            !voiture.getStatus().equalsIgnoreCase("En maintenance")) {
            throw new IllegalArgumentException("Invalid status. Allowed values are: Disponible, En cours, En maintenance");
        }

        // Sauvegarder et retourner la voiture
        return voitureRepository.save(voiture);
    }
    // Méthode pour supprimer une voiture
    public boolean deleteVoiture(Long id) {
        if (voitureRepository.existsById(id)) {
            voitureRepository.deleteById(id);
            return true;
        }
        return false;
    }
 // Méthode pour récupérer une voiture par son ID
    public Voiture getVoitureById(Long id) {
        return voitureRepository.findById(id).orElse(null);
    }
    public Iterable<Voiture> getAllVoitures() {
        return voitureRepository.findAll(); // Assuming you have a repository for Voiture
    }
    public Map<String, Object> getAllVoituresWithPagination(int page, int size) {
        // Utiliser PageRequest pour gérer la pagination
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Voiture> voiturePage = voitureRepository.findAll(pageRequest);

        // Préparer le résultat
        Map<String, Object> result = new HashMap<>();
        result.put("voitures", voiturePage.getContent()); // Liste des voitures de la page actuelle
        result.put("totalElements", voiturePage.getTotalElements()); // Nombre total d'éléments
        result.put("totalPages", voiturePage.getTotalPages()); // Nombre total de pages
        result.put("currentPage", voiturePage.getNumber()); // Page actuelle

        return result;
    }
}
