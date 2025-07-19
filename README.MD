# City Issue Reporting System

This is the backend system for a structured issue reporting platform designed for my city.  
It was proposed as a way to solve the problem of **unstructured citizen issue reports**, and the solution was **accepted as a pilot MVP**.

> This is an MVP — the foundation of the project. Future improvements are expected.

---

## Responsibility

I was fully responsible for designing and implementing the **backend architecture**,  
handling everything from database modeling and API design to unit testing and documentation.

---

## Architecture

The project follows a **Clean Architecture** approach, which emphasizes clear separation of concerns.

- **Controller Layer** handles HTTP requests
- **DTOs** are used for input/output validation and transformation
- **Service Layer** contains core business logic
- **Repository Layer** handles data persistence with Spring Data JPA
- A **Logger** is used across layers to track key application behavior, monitor exceptions, and support debugging

This structure improves maintainability, testability, and scalability.

---

## Continuous Integration (CI)

A **GitHub Actions** workflow was added to run automated builds and tests on every push or pull request to the `main` branch.


---

## Testing

- Unit tests written using **JUnit 5** and **Mockito**
- Focused on service layer logic with mocked dependencies
- Tests include:
  - User registration and updates
  - Report creation, updates, and deletion
  - Edge cases and exception handling

---

## Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT authentication & authorization**
- **Swagger (Springdoc OpenAPI)** for API documentation
- **JUnit + Mockito** for unit testing
- **Postman** for manual testing

---

## API Documentation

> Swagger UI is available for interactive API testing.

---

## Future Improvements

- Advanced image storage using cloud providers (e.g., AWS S3)
- CD pipeline setup for deployment
- More granular role-based access control
- Other potential features as requested by city administration

---
