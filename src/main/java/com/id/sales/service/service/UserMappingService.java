package com.id.sales.service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.id.sales.service.model.UserMapping;
import com.id.sales.service.repository.UserMappingRepository;

@Service
public class UserMappingService {

    @Autowired
    private UserMappingRepository userMappingRepository;

    public List<UserMapping> getAllUserMappings() {
        return userMappingRepository.findAll();
    }

    public Optional<UserMapping> getUserMappingById(UUID userMappingId) {
        return userMappingRepository.findById(userMappingId);
    }
    
    public Optional<UserMapping> getUserMappingByUsersId(String userMappingId) {
        return userMappingRepository.findByUserId(userMappingId);
    }

    public UserMapping createUserMapping(UserMapping userMapping) {
        return userMappingRepository.save(userMapping);
    }

    public UserMapping updateUserMapping(UUID userMappingId, UserMapping updatedUserMapping) {
        if (userMappingRepository.existsById(userMappingId)) {
            updatedUserMapping.setId(userMappingId);
            return userMappingRepository.save(updatedUserMapping);
        }
        return null; // Handle not found case
    }

    public void deleteUserMapping(UUID userMappingId) {
        userMappingRepository.deleteById(userMappingId);
    }
}
