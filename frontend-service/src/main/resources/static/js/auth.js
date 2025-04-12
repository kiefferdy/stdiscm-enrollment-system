// Auth utilities - Using Server-Side Sessions + Embedded Token for Client-Side API Calls

// Axios request interceptor to add Authorization header from embedded token
axios.interceptors.request.use(
    config => {
        // Check if the global token variable exists (set by Thymeleaf in layout.html)
        if (window.jwtToken) {
            config.headers['Authorization'] = `Bearer ${window.jwtToken}`;
            console.log("Axios interceptor added Authorization header.");
        } else {
             console.log("Axios interceptor: window.jwtToken not found.");
        }
        return config;
    },
    error => {
        console.error("Axios request interceptor error:", error);
        return Promise.reject(error);
    }
);

// Logout function - simply redirects to the server logout endpoint
function logout() {
    // Server handles session invalidation and cookie removal
    window.location.href = '/logout'; 
}

console.log("auth.js loaded (simplified for server-side sessions)");
