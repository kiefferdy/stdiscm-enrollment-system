-- Create necessary databases for our services
CREATE DATABASE IF NOT EXISTS enrollment_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS enrollment_course CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS enrollment_grade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS enrollment_profile CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant permissions to root user for all databases
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;
