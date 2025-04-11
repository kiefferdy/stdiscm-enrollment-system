package com.stdiscm.profile.service;

import com.stdiscm.profile.model.UserProfile;
import com.stdiscm.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public UserProfile saveProfile(Long userId, UserProfile profileDetails) {
        // Ensure the userId in the path matches the userId in the body
        if (!userId.equals(profileDetails.getUserId())) {
            throw new IllegalArgumentException("User ID in path (" + userId + ") does not match User ID in body (" + profileDetails.getUserId() + ")");
        }

        // Check if profile already exists for this userId
        Optional<UserProfile> existingProfileOpt = userProfileRepository.findByUserId(userId);

        if (existingProfileOpt.isPresent()) {
            // Update existing profile
            UserProfile existingProfile = existingProfileOpt.get();
            
            // Update mutable fields from the incoming profileDetails
            existingProfile.setUserType(profileDetails.getUserType()); 
            existingProfile.setFirstName(profileDetails.getFirstName());
            existingProfile.setLastName(profileDetails.getLastName());
            existingProfile.setPreferredName(profileDetails.getPreferredName());
            existingProfile.setDateOfBirth(profileDetails.getDateOfBirth());
            existingProfile.setGender(profileDetails.getGender());
            existingProfile.setPrimaryEmail(profileDetails.getPrimaryEmail());
            existingProfile.setSecondaryEmail(profileDetails.getSecondaryEmail());
            existingProfile.setMobilePhone(profileDetails.getMobilePhone());
            existingProfile.setAddressStreet(profileDetails.getAddressStreet());
            existingProfile.setAddressCity(profileDetails.getAddressCity());
            existingProfile.setAddressState(profileDetails.getAddressState());
            existingProfile.setAddressZipCode(profileDetails.getAddressZipCode());
            existingProfile.setAddressCountry(profileDetails.getAddressCountry());
            existingProfile.setEmergencyContactName(profileDetails.getEmergencyContactName());
            existingProfile.setEmergencyContactRelationship(profileDetails.getEmergencyContactRelationship());
            existingProfile.setEmergencyContactPhone(profileDetails.getEmergencyContactPhone());
            existingProfile.setStudentId(profileDetails.getStudentId());
            existingProfile.setMajor(profileDetails.getMajor());
            existingProfile.setEmployeeId(profileDetails.getEmployeeId());
            existingProfile.setDepartment(profileDetails.getDepartment());
            existingProfile.setTitle(profileDetails.getTitle());
            // Note: profileId, userId, createdAt are not updated
            // Note: updatedAt is updated automatically by @UpdateTimestamp

            return userProfileRepository.save(existingProfile);
        } else {
            // Create new profile
            // Note: createdAt and updatedAt will be set automatically by JPA annotations
            return userProfileRepository.save(profileDetails);
        }
    }
}
