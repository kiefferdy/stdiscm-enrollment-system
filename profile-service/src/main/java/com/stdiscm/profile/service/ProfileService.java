package com.stdiscm.profile.service;

import com.stdiscm.profile.model.UserProfile;
import java.util.Optional;

public interface ProfileService {

    /**
     * Retrieves the profile for a given user ID.
     *
     * @param userId The ID of the user whose profile is to be retrieved.
     * @return An Optional containing the UserProfile if found, otherwise empty.
     */
    Optional<UserProfile> getProfileByUserId(Long userId);

    /**
     * Creates or updates a user profile.
     * If a profile for the given userId exists, it updates it.
     * Otherwise, it creates a new profile.
     *
     * @param userId The ID of the user whose profile is being saved.
     * @param profileDetails The UserProfile object containing the details to save.
     *                       The userId in this object should match the userId parameter.
     * @return The saved UserProfile.
     * @throws IllegalArgumentException if the userId in profileDetails does not match the userId parameter.
     */
    UserProfile saveProfile(Long userId, UserProfile profileDetails);

}
