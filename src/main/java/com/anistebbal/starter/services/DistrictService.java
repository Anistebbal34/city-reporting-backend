package com.anistebbal.starter.services;

import com.anistebbal.starter.dto.DistrictResponseDTO;
import com.anistebbal.starter.dto.StreetResponseDTO;
import com.anistebbal.starter.entities.City;
import com.anistebbal.starter.entities.District;
import com.anistebbal.starter.entities.Street;
import com.anistebbal.starter.repositories.CityRepository;
import com.anistebbal.starter.repositories.DistrictRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DistrictService {

        @Autowired
        private DistrictRepository districtRepository;

        @Autowired
        private CityRepository cityRepository;

        // ✅ Get all districts for a given city
        public List<DistrictResponseDTO> getDistrictDTOsByCity(Long cityId) {
                return districtRepository.findByCityId(cityId)
                                .stream()
                                .map(this::mapToDistrictDto)
                                .collect(Collectors.toList());
        }

        // ✅ Create new district
        public DistrictResponseDTO createDistrict(String name, Long cityId) {
                City city = cityRepository.findById(cityId)
                                .orElseThrow(() -> new EntityNotFoundException("City not found"));

                District district = new District();
                district.setName(name);
                district.setCity(city);

                District saved = districtRepository.save(district);
                return new DistrictResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                city.getId(),
                                city.getName(),
                                List.of() // no streets yet
                );
        }

        // ✅ Update district name or city
        public DistrictResponseDTO updateDistrict(Long districtId, String newName, Long newCityId) {
                District district = districtRepository.findById(districtId)
                                .orElseThrow(() -> new EntityNotFoundException("District not found"));

                if (newName != null && !newName.trim().isEmpty()) {
                        district.setName(newName);
                }

                if (newCityId != null) {
                        City newCity = cityRepository.findById(newCityId)
                                        .orElseThrow(() -> new EntityNotFoundException("City not found"));
                        district.setCity(newCity);
                }

                District updated = districtRepository.save(district);
                return mapToDistrictDto(updated);
        }

        // ✅ Private helper: Convert Street -> StreetResponseDTO
        private StreetResponseDTO mapToStreetDto(Street street, District district) {
                return new StreetResponseDTO(
                                street.getId(),
                                street.getName(),
                                district.getId(),
                                district.getName());
        }

        // ✅ Private helper: Convert District -> DistrictResponseDTO
        private DistrictResponseDTO mapToDistrictDto(District district) {
                List<StreetResponseDTO> streetDtos = district.getStreets()
                                .stream()
                                .map(street -> mapToStreetDto(street, district))
                                .toList();

                return new DistrictResponseDTO(
                                district.getId(),
                                district.getName(),
                                district.getCity().getId(),
                                district.getCity().getName(),
                                streetDtos);
        }
}
