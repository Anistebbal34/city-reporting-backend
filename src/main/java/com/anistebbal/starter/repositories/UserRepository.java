package com.anistebbal.starter.repositories;

import com.anistebbal.starter.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);

    List<User> findByStreetId(Long streetId);

    List<User> findByUsername(String username);

    List<User> findByStreetDistrictId(Long districtId);

}
