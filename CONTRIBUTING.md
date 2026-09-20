# Contributing to PrakashMart

This document outlines the workflow, coding standards, and branch strategy for contributing to the **PrakashMart** project.

---

## 1. Quick Start

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/PrakashMart.git
   cd PrakashMart
   ```

2. **Configure Local Environment**:
   ```bash
   cp src/main/resources/config.properties.example src/main/resources/config.properties
   ```

3. **Build the Project & Run Automated Tests**:
   ```bash
   mvn clean verify
   ```

4. **Package Production WAR File**:
   ```bash
   mvn clean package -DskipTests
   ```

---

## 2. Architectural Guidelines & Constraints

- **Strict Framework Restrictions**: Use only pure Java EE Servlet 4.0, raw JDBC with HikariCP, and JSP/JSTL. Do not introduce Spring, Hibernate, JPA, or frontend JS frameworks.
- **Transactional Safety**: All multi-step database mutations (such as checkout operations) must be wrapped in atomic JDBC transactions (`conn.setAutoCommit(false)`, `conn.commit()`, and `rollbackTransaction()`).
- **Security by Design**:
  - All SQL queries must use parameterized `PreparedStatement` with bind variables. String concatenation in SQL is prohibited.
  - All user-supplied output rendered in JSP must be escaped with JSTL `<c:out value="..."/>`.
  - State-changing endpoints must enforce CSRF token validation.
  - Passwords must be hashed using BCrypt (cost factor 12).
- **Error Handling**: Use custom application exceptions (`AppException` hierarchy). Never expose raw database exceptions or stack traces to client responses.

---

## 3. Git Workflow & Branching

- `main` / `master`: Production-ready, deployable code. All tests must pass before merging.
- Feature branches: `feature/<short-description>`.
- Commit format: Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
- CI/CD: Every pull request automatically triggers GitHub Actions (`mvn -B clean verify`).
