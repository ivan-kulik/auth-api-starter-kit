## Auth API Starter Kit

This repository contains a production-ready starter template for building secure authentication and authorization  
services in a **Spring Boot** application, using **PostgreSQL** as the primary DBMS.

### Purpose

The main goal of this project is to provide a production-ready template with:

- Secure user registration and email verification.
- JWT-based stateless authentication with access and refresh token rotation.
- OAuth2 login integration.
- Secure logout with token invalidation and blacklisting.
- Rate limiting, CORS configuration, and other security best practices.

### Technology Stack

- Language: Java
- Framework: Spring Boot
- ORM: Hibernate via Spring Data JPA
- Security: Spring Security, JWT
- DBMS: PostgreSQL
- Build Tool: Gradle
- Containerization: Docker, Docker Compose

### Getting Started

#### Prerequisites

- Java Development Kit (JDK) 21 or later.
- Docker and Docker Compose installed on your machine.

#### Local Development (with Docker Compose for PostgreSQL)

1. **Clone the repository**:
    ```bash
    git clone https://github.com/ivan-kulik/auth-api-starter-kit.git <your_project_name>
    cd <your_project_name>
    ```

2. **Start the infrastructure services**:
   Start PostgreSQL and app containers. This setup uses named volumes to ensure your data persists even 
   if the containers are stopped or removed.
   ```bash
   docker compose up -d
   ```
   Infrastructure management commands:

    * Stop the containers:
      ```bash
      docker compose stop
      ```
    * Remove the containers (data is preserved in volumes):
      ```bash
      docker compose down
      ```
    * Reset all data (delete volumes):
      ```bash
      docker compose down -v
      ```

3. **Configure the application**:
   Rename `application-example.yml` to `application.yml` and update the following:
    - Database connection settings for PostgreSQL.
    - SMTP server settings for email verification.
    - JWT signing secrets and token expiration values.
    - OAuth2 client credentials (if using social login).

4. **Build the application**:
    ```bash
    ./gradlew clean build
    ```

5. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

#### Migrating to a Production Service

After downloading the starter kit, adapt it to your new project (for example, `user-management-service` or `online-store-backend`)  
by performing the following changes:

1. **Global rename of package and project**

- **Update Gradle coordinates**: Open `build.gradle` and change the `group` (e.g., `com.mycompany`) and `description` to `your_project_name`. 
Then open `settings.gradle` and change `rootProject.name` to `your_project_name` (match your organization and service name).
- **Rename the root package**: In your IDE, rename `com.starter` to your desired base package (e.g., `com.mycompany.newapp`).
- **Rename the main application class**: Change `AuthApiStarterKitApplication.java` (and its test counterpart in `src/test`)  
to `<YourService>Application.java`.

2. **Configuration and security hardening**

- Update CORS allowed origins to match your production frontend domain.
- Review and customize email templates for verification flows.
- Adjust rate-limiting thresholds to match your expected traffic.


### API Documentation

### Usage
