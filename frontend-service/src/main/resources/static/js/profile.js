/**
 * Fetches the user profile for the currently logged-in user.
 * Assumes userId is stored in localStorage after login.
 * Requires auth.js for isAuthenticated and axios interceptors.
 */
async function fetchUserProfile() {
    if (!isAuthenticated()) {
        console.error("User not authenticated. Cannot fetch profile.");
        return null;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) {
        console.error("User ID not found in localStorage.");
        return null;
    }

    try {
        // Axios interceptor in auth.js will add the Authorization header
        const response = await axios.get(`/api/profiles/${userId}`);
        if (response.data && response.data.success) {
            return response.data.data;
        } else {
            console.error("Failed to fetch profile:", response.data ? response.data.message : 'Unknown error');
            return null;
        }
    } catch (error) {
        console.error("Error fetching user profile:", error.response ? error.response.data : error.message);
        // auth.js interceptor might handle 401 redirection automatically
        return null;
    }
}

/**
 * Updates the user profile for the currently logged-in user.
 * Assumes userId is stored in localStorage.
 * Requires auth.js for isAuthenticated and axios interceptors.
 *
 * @param {object} profileData The updated profile data object. Must include userId matching logged-in user.
 */
async function updateUserProfile(profileData) {
    if (!isAuthenticated()) {
        console.error("User not authenticated. Cannot update profile.");
        return false;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) {
        console.error("User ID not found in localStorage.");
        return false;
    }

    // Ensure the profileData contains the correct userId
    if (!profileData || profileData.userId !== parseInt(userId)) {
         console.error("Profile data is missing or userId mismatch.");
         alert("Error: Profile data mismatch. Cannot update.");
         return false;
    }


    try {
        // Axios interceptor adds Auth header
        const response = await axios.put(`/api/profiles/${userId}`, profileData);
        if (response.data && response.data.success) {
            console.log("Profile updated successfully:", response.data.data);
            alert("Profile updated successfully!");
            return true;
        } else {
            console.error("Failed to update profile:", response.data ? response.data.message : 'Unknown error');
            alert(`Failed to update profile: ${response.data?.message || 'Unknown error'}`);
            return false;
        }
    } catch (error) {
        console.error("Error updating user profile:", error.response ? error.response.data : error.message);
        alert(`Error updating profile: ${error.response?.data?.message || error.message}`);
        // auth.js interceptor might handle 401
        return false;
    }
}
