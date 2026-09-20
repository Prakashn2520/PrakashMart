# Sprint Retrospectives (RETRO.md)

Project Retrospectives documenting lessons learned, operational insights, and architectural decisions across all development sprints.

---

### Sprint 1: Database Architecture, DAO & Core Services
- **What worked**: Raw JDBC with HikariCP connection pooling delivered high throughput and clear transactional boundaries for inventory decrement during checkout.
- **What didn't**: In-memory test databases required strict migration lifecycle management so that tests didn't leak state.
- **One change for next sprint**: Created dedicated `DatabaseMigrationRunner` and in-memory test harnesses to ensure clean schema initialization before each test run.

---

### Sprint 2: Security Pipeline & Authentication
- **What worked**: `AuthFilter` and `CsrfFilter` cleanly segregated authentication checks and token verification without polluting servlets.
- **What didn't**: Session fixation defense needed explicit alignment with Tomcat's session manager to ensure new session cookies were properly emitted.
- **One change for next sprint**: Standardized on `request.getSession(true)` with session attribute transfer and explicit cookie tracking in HTTP test helpers.

---

### Sprint 3: Frontend UI, Classical Design System & Business Flows
- **What worked**: Classical, restrained styling using CSS variables and modular JSPs resulted in a fast, elegant UI without heavy frontend dependencies.
- **What didn't**: JSP EL string comparisons for enum values required exact matching of enum constant names to prevent Jasper coercion exceptions.
- **One change for next sprint**: Added comprehensive end-to-end integration tests (`EndToEndBusinessFlowsTest`) running against embedded Tomcat to catch JSP rendering and session issues before production build.
