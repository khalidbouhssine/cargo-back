package com.car.cargo.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.car.cargo.models.Client;
import com.car.cargo.repository.ClientRepository;
import com.car.cargo.repository.ReclamationRepository;
import com.car.cargo.repository.ReservationRepository;

@Service
public class ClientService {
	
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ReclamationRepository reclamationRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;

    public Client addClient(Client client) {
        return clientRepository.save(client); // Enregistre le client dans la base de données
    }
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }
    @Autowired
    private final BCryptPasswordEncoder encoder;
    public ClientService(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }
    public Map<String, Object> getClientsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Client> clientsPage = clientRepository.findAll(pageable);

        // Créer une liste contenant uniquement les champs désirés
        List<Map<String, Object>> clientsList = clientsPage.getContent().stream()
            .map(client -> {
                Map<String, Object> clientMap = new HashMap<>();
                clientMap.put("idClient", client.getIdClient());
                clientMap.put("nomComplet", client.getNomComplet());
                clientMap.put("city", client.getCity());
                clientMap.put("cin", client.getCin());
                clientMap.put("email", client.getEmail());
                return clientMap;
            })
            .collect(Collectors.toList());

        // Créer la réponse avec la pagination
        Map<String, Object> result = new HashMap<>();
        result.put("clients", clientsList);
        result.put("totalElements", clientsPage.getTotalElements());
        result.put("totalPages", clientsPage.getTotalPages());

        return result;
    }
    
    
    public boolean deleteClientById(Long id) {
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public int getReservationCountByClient(Client client) {
        return reservationRepository.countByIdClient(client);
    }
    
    public int getReclamationCountByClient(Client client) {
        return reclamationRepository.countByClient(client);
    }

}
