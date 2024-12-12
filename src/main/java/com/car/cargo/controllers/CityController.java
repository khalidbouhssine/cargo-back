package com.car.cargo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.car.cargo.models.City;
import com.car.cargo.services.CityService;

import java.util.Map;

@RestController
@RequestMapping("/city")
public class CityController {

    @Autowired
    private CityService cityService;

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
    // Route to fetch all cities
    @GetMapping("/displayallcity")
    public ResponseEntity<?> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }
}
