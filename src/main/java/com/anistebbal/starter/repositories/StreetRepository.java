package com.anistebbal.starter.repositories;

import com.anistebbal.starter.entities.Street;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StreetRepository extends JpaRepository<Street, Long> {
    List<Street> findByDistrictId(Long districtId);

}
