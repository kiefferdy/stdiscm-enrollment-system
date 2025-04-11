package com.stdiscm.profile.repository;

import com.stdiscm.profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Finds a user profile by the associated user ID (from auth-service).
     *
     * @param userId The user ID.
     * @return An Optional containing the UserProfile if found, otherwise empty.
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Checks if a profile exists for the given user ID.
     *
     * @param userId The user ID.
     * @return true if a profile exists, false otherwise.
     */
    boolean existsByUserId(Long userId);
}
