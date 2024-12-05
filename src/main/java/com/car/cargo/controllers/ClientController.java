package com.car.cargo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.car.cargo.models.Client;
import com.car.cargo.services.ClientService;

@RestController
@RequestMapping("/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Méthode POST pour ajouter un client
    @PostMapping("/registerClient")
    public Client createUser(@RequestBody Client client) {
    	 // Chiffrement du mot de passe avant l'enregistrement
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        return clientService.addClient(client);
    }
}
