// Auth utilities

// Set authorization header for all axios requests
axios.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// Handle 401 Unauthorized responses
axios.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        if (error.response && error.response.status === 401) {
            // Redirect to login if unauthorized
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            localStorage.removeItem('username');
            localStorage.removeItem('userRoles');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// Check if user is authenticated
function isAuthenticated() {
    return !!localStorage.getItem('token');
}

// Check if user has a specific role
function hasRole(role) {
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    return userRoles.includes(role);
}

// Logout function
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('userRoles');
    window.location.href = '/';
}

// Redirect to login if not authenticated
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// Redirect to dashboard if already authenticated
function redirectIfAuthenticated() {
    if (isAuthenticated()) {
        window.location.href = '/dashboard';
        return true;
    }
    return false;
}
