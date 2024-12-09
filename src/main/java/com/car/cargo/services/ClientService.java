package com.car.cargo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.car.cargo.models.Client;
import com.car.cargo.repository.ClientRepository;

@Service
public class ClientService {
	
    @Autowired
    private ClientRepository clientRepository;

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
    
}
