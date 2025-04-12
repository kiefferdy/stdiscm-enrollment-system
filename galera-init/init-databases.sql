-- Create databases for each service
CREATE DATABASE IF NOT EXISTS enrollment_auth;
CREATE DATABASE IF NOT EXISTS enrollment_course;
CREATE DATABASE IF NOT EXISTS enrollment_grade;
CREATE DATABASE IF NOT EXISTS enrollment_profile;

-- Grant privileges to root user (already has access, but being explicit)
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;
