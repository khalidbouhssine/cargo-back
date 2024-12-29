package com.car.cargo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.car.cargo.models.City;
import com.car.cargo.repository.CityRepository;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    // Add a new city if it doesn't already exist
    public City addCity(String nameCity) {
        if (cityRepository.existsByNameCity(nameCity)) {
            throw new IllegalArgumentException("City with this name already exists");
        }

        City city = new City();
        city.setNameCity(nameCity);
        return cityRepository.save(city);
    }
   
 // remove city by Id
    public boolean deleteCityById(Long id) {
        if (!cityRepository.existsById(id)) {
            return false; // City does not exist
        }

        cityRepository.deleteById(id);
        return true; // City deleted successfully
    }
 // Fetch all cities
    public List<City> getAllCities() {
        return cityRepository.findAll(); // Assurez-vous que `findAll` retourne une List
    }
}
