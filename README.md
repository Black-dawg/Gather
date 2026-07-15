# Gather

Gather is a secure, high-concurrency event ticketing and validation platform. The backend is built using **Spring Boot** (Java 21) and **PostgreSQL**, while the frontend is built with **React** (Vite + TS + TailwindCSS v4). User authentication and security are handled through **Keycloak** (OAuth2/OIDC), and ticket validation happens in real-time using QR codes.

---

## How It Works (Architecture)

The project uses a standard three-tier architecture. We use Keycloak as our OAuth2 Identity Provider, PostgreSQL for the database, and run everything locally inside Docker containers.

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

### Running with Docker
The local services are defined in [docker-compose.yml](file:///c:/Users/Hp/Desktop/Event%20Ticket%20Platform/Backend/docker-compose.yml):
* **db**: PostgreSQL 16 running on port `5433` (mapped to `5432` internally).
* **keycloak**: Keycloak identity server running on port `9090`.
* **adminer**: Database administration console running on port `8888`.

---

## Database Schema

Database tables are mapped using JPA annotations. Hibernate handles schema updates automatically when the backend starts up (`spring.jpa.hibernate.ddl-auto=update`).

Here is how the tables are structured and connected:

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

## Features & Implementation Details

* **Auth & User Sync**: We use Keycloak OIDC for logins. When a user logs in for the first time, a custom filter extracts their profile details and automatically creates a user record in the local database.
* **Preventing Double Bookings**: To prevent selling more tickets than available, we use database-level pessimistic write locking (`SELECT ... FOR UPDATE`). We also track verification logs to ensure a ticket's QR code cannot be scanned twice.
* **Event Search**: We use PostgreSQL's native full-text search (`tsvector` / `tsquery`) with GIN indexing for fast, linguistics-aware search performance.

---

## Getting Started

1. **Start the Database & Auth**: Run `docker compose up -d` in the `Backend` folder to launch PostgreSQL, Keycloak, and Adminer.
2. **Start the Backend**: Run `./mvnw spring-boot:run` in the `Backend` folder. The API will start at `http://localhost:8080`.
3. **Start the Frontend**: Run `npm install` and then `npm run dev` in the `Frontend` folder to launch the React app at `http://localhost:5173`.

---

## App Screenshots

### 1. Attendee Homepage & Event Search
Where attendees can browse events and search through them using the full-text search bar.
<br>
<img src="images/userhomepage.png" width="800" alt="Attendee Homepage" />

### 2. Event Details & Ticket Options
View the event description, location, timing, and select ticket types.
<br>
<img src="images/user-event1.png" width="600" alt="Event Details" />

### 3. Checkout & Purchased Tickets
Purchase tickets and view them in the user profile dashboard.
<br>
<img src="images/ticket-purchase1.png" width="48%" alt="Checkout Page" /> <img src="images/user%20ticketdashboard.png" width="48%" alt="Purchased Tickets List" />

### 4. Ticket Receipt with QR Code
A dynamic ticket showing the barcode/QR code generated by the backend's ZXing engine.
<br>
<img src="images/ticket1.png" width="400" alt="Ticket QR Code Detail" />

### 5. Organizer Event Dashboard
Where organizers can manage, publish, and edit their events.
<br>
<img src="images/organiser%20dashboard.png" width="500" alt="Organizer Dashboard" />

### 6. Entry Validation (Staff Scanner)
The scanning interface used by event staff to verify tickets in real-time.
<br>
<img src="images/qr1.png" height="380" alt="Scanner Camera Feed" />&nbsp;&nbsp;<img src="images/qr2.png" height="380" alt="Validation Success Screen" />


