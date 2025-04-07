# Distributed Enrollment System

A distributed online enrollment system built with Java and Spring Boot microservices architecture.

## Project Structure

The system is composed of multiple services, each running on its own node:

1. **Discovery Service** - Eureka service registry for service discovery
2. **API Gateway** - Spring Cloud Gateway for routing requests to appropriate services
3. **Authentication Service** - Handles user authentication with JWT
4. **Course Service** - Manages courses and enrollment
5. **Grade Service** - Manages student grades
6. **Frontend Service** - Provides the user interface (MVC view layer)
7. **Database Services** - Redundant MySQL databases for each service

## Features

1. **Login/Logout** - User authentication with JWT, session tracking across nodes
2. **Course Management** - View available courses, course details
3. **Enrollment** - Students can enroll in open courses
4. **Grade Management** - Students can view grades, faculty can upload grades
5. **Distributed Architecture** - Each service runs on its own node with fault tolerance
6. **Database Redundancy** - Redundant databases for high availability

## Prerequisites

- Java 17
- Maven
- Docker & Docker Compose (for containerized deployment)
- MySQL (for local development)

## Building and Running

### Local Development

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Start the services in the following order:
   - Discovery Service
   - API Gateway
   - Auth Service
   - Course Service
   - Grade Service
   - Frontend Service

### Docker Deployment

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Run with Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. For redundant database configuration:
   ```bash
   docker-compose -f docker-compose-redundant.yml up -d
   ```

## Accessing the Application

- Web UI: http://localhost:8084
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761

## Project Overview

### Service Distribution

Each service is designed to run on a separate node, providing fault tolerance. If one node fails, only the functionality provided by that node becomes unavailable while the rest of the system continues to operate.

### Service Communication

Services communicate with each other via RESTful APIs. The API Gateway routes requests to the appropriate service based on the URL path.

### Database Redundancy

The system supports redundant databases for high availability. In the redundant configuration, each service connects to a primary and a secondary database. Read operations are directed to the secondary database, while write operations are performed on the primary database.

### Fault Tolerance

- If the Authentication Service fails, users will not be able to log in, but existing sessions will continue to work
- If the Course Service fails, users will not be able to view or enroll in courses
- If the Grade Service fails, users will not be able to view or submit grades
- If the Frontend Service fails, users will not be able to access the web interface
- If the API Gateway fails, client requests will not be routed to the appropriate services
- If the Discovery Service fails, new service instances will not be discoverable

### Session Management

User sessions are maintained across nodes using JWT tokens, which are stored in the browser's local storage. Each request to any service includes the JWT token in the Authorization header, allowing the service to authenticate the user.

## Implementation Details

### Technologies Used

- Spring Boot
- Spring Cloud Netflix (Eureka)
- Spring Cloud Gateway
- Spring Security & JWT
- Spring Data JPA
- Thymeleaf for server-side rendering
- Bootstrap for UI
- MySQL for data persistence
- Docker & Docker Compose for containerization

### Architecture

```
Client -> API Gateway -> Services (Auth, Course, Grade) -> Databases
                      -> Frontend Service (UI)
```

All services register with the Discovery Service, which enables dynamic service discovery and load balancing.
