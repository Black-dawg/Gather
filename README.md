# Gather

A secure, high-concurrency event ticketing and validation platform built with **Spring Boot** (Java 21), **PostgreSQL**, and **React** (Vite + TS + TailwindCSS v4), featuring authentication via **Keycloak** (OAuth2/OIDC) and real-time QR code ticket validation.

---

## 🏗️ Architectural System Overview

The platform uses a classic **Three-Tier Architecture** integrated with a central OAuth2 Identity Provider (Keycloak) and a relational database (PostgreSQL), all orchestrated inside Docker containers for local development.

```mermaid
graph TD
    Client[React Web Client] -->|HTTP Request + JWT| Gateway[Spring Security OAuth2 Resource Server]
    Gateway -->|1. Validate JWT| Keycloak[Keycloak Identity Provider]
    Gateway -->|2. Extract Roles & Claims| AuthConv[JwtAuthenticationConverter]
    Gateway -->|3. Auto-provision User| ProvFilter[UserProvisioningFilter]
    ProvFilter -->|Check & Save| DB[(PostgreSQL Database)]
    
    Gateway -->|4. Route Request| Controller[Controller Layer]
    Controller -->|DTOs| Service[Service Layer]
    Service -->|Entities / Locks| Repo[Spring Data JPA Repositories]
    Repo --> DB
    
    Service -->|QR Generation| ZXing[ZXing QR Engine]
```

### Infrastructure Services (Docker)
The local development infrastructure is orchestrated using Docker Compose (defined in [docker-compose.yml](file:///c:/Users/Hp/Desktop/Event%20Ticket%20Platform/Backend/docker-compose.yml)):
* **db**: PostgreSQL 16 exposed on port `5433` (mapped to `5432` internally).
* **keycloak**: Keycloak identity server running on port `9090`.
* **adminer**: Database administration console running on port `8888`.

---

## 🗄️ Database Domain & ER Model

Database schemas are mapped using JPA annotations with automatic updates enabled via Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

```mermaid
erDiagram
    USERS {
        uuid id PK "Keycloak Subject UUID"
        varchar name "preferred_username"
        varchar email "email"
        timestamp created_at
        timestamp updated_at
    }
    EVENTS {
        uuid id PK "Generated UUID"
        varchar name
        varchar venue
        timestamp event_start
        timestamp event_end
        timestamp sales_start
        timestamp sales_end
        varchar status "DRAFT/PUBLISHED/CANCELLED/COMPLETED"
        uuid organizer_id FK
        timestamp created_at
        timestamp updated_at
    }
    TICKET_TYPES {
        uuid id PK "Generated UUID"
        varchar name
        numeric price
        varchar description
        integer total_available
        uuid event_id FK "Cascade Delete on Event"
        timestamp created_at
        timestamp updated_at
    }
    TICKETS {
        uuid id PK "Generated UUID"
        varchar status "PURCHASED/CANCELLED"
        uuid ticket_type_id FK
        uuid purchaser_id FK
        timestamp created_at
        timestamp updated_at
    }
    QR_CODES {
        uuid id PK "Matches Ticket UUID"
        varchar status "ACTIVE/EXPIRED"
        text value "Base64 PNG Image Data"
        uuid ticket_id FK
        timestamp created_at
        timestamp updated_at
    }
    TICKET_VALIDATIONS {
        uuid id PK "Generated UUID"
        varchar status "VALID/INVALID/EXPIRED"
        varchar validation_method "QR_SCAN/MANUAL"
        uuid ticket_id FK
        timestamp created_at
        timestamp updated_at
    }

    USERS ||--o{ EVENTS : "organizes"
    EVENTS ||--o{ TICKET_TYPES : "owns (orphanRemoval)"
    TICKET_TYPES ||--o{ TICKETS : "tracks capacity"
    TICKETS ||--|| USERS : "purchased by"
    TICKETS ||--o{ QR_CODES : "contains"
    TICKETS ||--o{ TICKET_VALIDATIONS : "has scan history"
```

---

## 🔒 Security & Core Mechanics
* **Authentication & Provisioning**: Secured using Keycloak OIDC JWT tokens, with custom role mapping and automated user provisioning in the local database upon first login.
* **Concurrency & Verification**: Employs database-level pessimistic write locking (`SELECT ... FOR UPDATE`) to prevent overselling, and checks validation history to block duplicate scans.
* **Search Engine**: Uses native PostgreSQL full-text search (`tsvector` / `tsquery`) with GIN indexing for fast, linguistics-aware search performance.

---

## 🚀 Setup & Run Instructions

1. **Start Infrastructure**: Run `docker compose up -d` in the `Backend` folder to launch PostgreSQL, Keycloak, and Adminer.
2. **Launch Backend**: Run `./mvnw spring-boot:run` in the `Backend` folder to start the Spring Boot API at `http://localhost:8080`.
3. **Launch Frontend**: Run `npm install` and `npm run dev` in the `Frontend` folder to start the React web application at `http://localhost:5173`.

---

## 📸 Application Screens & User Flows

### 1. Attendee Homepage & Public Event Search
*Browse published events and query events using native full-text search.*
<br>
<img src="images/userhomepage.png" width="800" alt="Attendee Homepage" />

### 2. Event Details & Ticket Options
*View event descriptions, metadata, and available ticket types.*
<br>
<img src="images/user-event1.png" width="600" alt="Event Details" />

### 3. Ticket Checkout & Purchase List
*Secure purchase page and list of purchased tickets under the attendee's profile.*
<br>
<img src="images/ticket-purchase1.png" width="48%" alt="Checkout Page" /> <img src="images/user%20ticketdashboard.png" width="48%" alt="Purchased Tickets List" />

### 4. Ticket Receipt with QR Code
*Receipt showing ticket details and a dynamic PNG QR Code generated by the backend's ZXing engine.*
<br>
<img src="images/ticket1.png" width="400" alt="Ticket QR Code Detail" />

### 5. Organizer Event Dashboard
*Dashboard allowing organizers to create, publish, modify, or delete their events.*
<br>
<img src="images/organiser%20dashboard.png" width="500" alt="Organizer Dashboard" />

### 6. Staff Ticket Scanner
*Camera-driven scanner interface checking tickets against the backend API to prevent duplicate entries.*
<br>
<img src="images/qr1.png" height="380" alt="Scanner Camera Feed" />&nbsp;&nbsp;<img src="images/qr2.png" height="380" alt="Validation Success Screen" />
