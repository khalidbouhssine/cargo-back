package com.car.cargo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.car.cargo.models.AdminGlobal;
import com.car.cargo.models.AdminLocal;
import com.car.cargo.models.City;
import com.car.cargo.models.Client;
import com.car.cargo.services.AdminGlobalService;
import com.car.cargo.services.AdminLocalService;
import com.car.cargo.services.CityService;
import com.car.cargo.services.ClientService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/city")
@CrossOrigin(origins = "*")
public class CityController {

    @Autowired
    private CityService cityService;
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private AdminLocalService adminLocalService;

    @Autowired
    private AdminGlobalService adminGlobalService;
    
    private static final String SECRET_KEY = "VFAbCGus7Mr0laauDiYfHsNgkUHXfgaok10ior2lYwxsuetda/uf4l4QYzfGtAyxylRFGpkzfMR44Vey0qGcUg==";

 // Route to create a new city
    @PostMapping("/create")
    public ResponseEntity<?> createCity(@RequestBody Map<String, String> requestBody) {
        String nameCity = requestBody.get("nameCity");

        if (nameCity == null || nameCity.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "City name is required"));
        }

        try {
            City createdCity = cityService.addCity(nameCity);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    // Route to delete a city by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCityById(@PathVariable("id") Long id) {
        boolean isDeleted = cityService.deleteCityById(id);

        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "City not found"));
        }

        return ResponseEntity.ok(Map.of("message", "City deleted successfully"));
    }
    @GetMapping("/displayallcity")
    public ResponseEntity<?> getCitiesByToken(@RequestHeader("Authorization") String token) {
        try {
            // Vérifier et décoder le token
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes()) // Utilisation de la clé secrète
                    .parseClaimsJws(token.replace("Bearer ", "")) // Retirer le préfixe "Bearer "
                    .getBody();

            // Récupérer l'email depuis le token
            String email = claims.getSubject();

            // Vérifier si un client, adminLocal ou adminGlobal existe avec cet email
            Client client = clientService.findByEmail(email);
            AdminLocal adminLocal = adminLocalService.findByEmail(email);
            AdminGlobal adminGlobal = adminGlobalService.findByEmail(email);

            if (client == null && adminLocal == null && adminGlobal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token or no user found (Client, AdminLocal, AdminGlobal)"));
            }

            // Si un des utilisateurs est trouvé, récupérer toutes les villes (id et nameCity uniquement)
            List<Map<String, Object>> cities = cityService.getAllCities().stream()
                    .map(city -> {
                        Map<String, Object> cityMap = new HashMap<>();
                        cityMap.put("id", city.getIdCity());
                        cityMap.put("nameCity", city.getNameCity());
                        return cityMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(cities);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token or unable to process token"));
        }
    }

}
