package com.car.cargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.car.cargo.models.City;

public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByNameCity(String nameCity);
}
