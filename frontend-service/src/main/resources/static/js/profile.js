/**
 * Updates the user profile for the currently logged-in user.
 * Relies on server-side session for authentication.
 * Axios interceptor in auth.js adds the Authorization header.
 *
 * @param {object} profileData The updated profile data object. Must include userId.
 */
async function updateUserProfile(profileData) {
    // Basic check if profileData and userId exist
    if (!profileData || !profileData.userId) {
         console.error("Profile data or userId is missing.");
         alert("Error: Cannot update profile without user ID.");
         return false;
    }
    
    const userIdToUpdate = profileData.userId;

    try {
        // Axios interceptor in auth.js adds Auth header
        const response = await axios.put(`/api/profiles/${userIdToUpdate}`, profileData);
        // Assuming API returns the updated profile DTO within response.data.data
        if (response.data && (response.data.success !== undefined ? response.data.success : true)) { 
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
        return false;
    }
}
