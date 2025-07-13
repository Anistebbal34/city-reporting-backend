package com.anistebbal.starter.repositories;

import com.anistebbal.starter.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByName(String name);
}
