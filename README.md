#  Banking Web Application

A **banking web application** built with **Java Spring Boot (Maven)** for the backend and **plain HTML, CSS, and JavaScript** for the frontend.

---

##  Overview

The project consists of two main parts:

1. **Backend** – Built with Spring Boot using Java and Maven (packaged as JAR)
2. **Frontend** – Built using plain HTML, CSS, and JavaScript

The backend handles:
- User authentication (with password hashing)
- Money transfers & account management
- Database CRUD operations via REST APIs

The frontend is a lightweight static interface that communicates with the backend via HTTP (AJAX/Fetch).

---

##  Tech Stack

| Layer | Technology Used |
|--------|------------------|
| **Backend Framework** | Spring Boot 3.5.7 |
| **Language** | Java 17+ |
| **Build Tool** | Maven |
| **Packaging Type** | JAR |
| **Configuration Format** | `.properties` |
| **Database** | MySQL |
| **ORM** | Spring Data JPA |
| **Frontend** | HTML, CSS, JavaScript |
| **Password Hashing** | BCrypt (Spring Security Crypto) |

---

##  Spring Initializr Configuration

Generated from [https://start.spring.io](https://start.spring.io) with the following options:

| Setting | Value |
|----------|--------|
| Project | Maven |
| Language | Java |
| Spring Boot Version | 3.5.7 |
| Packaging | JAR |
| Java Version | 21 |
| Group | `com.bankapp` |
| Artifact | `bankapp-backend` |

### Dependencies Selected:
- **Spring Web**
- **Spring Data JPA**
- **MySQL Driver**
- **Spring Boot DevTools**
- **Validation**
- **Spring Security**

---

## 📁 Folder Structure

```
BankingApplication/
├── bank-backend/                        ← Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bankapp/...     ← Java packages (controller, service, etc.)
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── static/
│   │   │       └── templates/
│   ├── database/
│   │   ├── schema.sql                   ← Database schema
│   │   └── seed.sql                     ← Initial seed data (dummy records)
│   ├── pom.xml
│   └── target/                          ← Compiled JAR and build output
│
├── bankapp-frontend/                    ← Frontend (static files)
│   ├── index.html
│   ├── js/
│   │   └── main.js
│   ├── css/
│   │   └── style.css
│   └── img/
│       └── (logos, icons, etc.)
│
└── README.md
```

---
##  Backend Configuration (`application.properties`)

Located at:  
`bankapp-backend/src/main/resources/application.properties`

---
## Running the Backend

The backend will run at:  
`http://localhost:8080`