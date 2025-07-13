# City Issue Reporting System - MVP Backend

This is the backend system for a structured issue reporting platform designed for my city.  
It was proposed as a way to solve the problem of **unstructured citizen issue reports**, and the solution was **accepted as a pilot MVP**.

> This is an MVP — the foundation of the project. Future improvements are expected.

---

## Responsibility

I was fully responsible for designing and implementing the **backend architecture**,  
handling everything from database modeling and API design to unit testing and documentation.

## Architecture

The project follows a **Clean Architecture** style using:

- **DTOs** for input/output validation and transformation
- **Service Layer** for business logic
- **Repository Layer** for data access
- **Controller Layer** for handling HTTP requests

This ensures maintainability, testability, and separation of concerns.

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
- CI/CD pipeline setup for deployment
- More granular role-based access control
- Other potential features as requested by city administration

---
