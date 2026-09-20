# Changelog

All notable changes to the PrakashMart project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-09-20 (Final Release)

### Added
- **Core Architecture**: Layered MVC application on Servlet 4.0, raw JDBC, HikariCP, and H2 database.
- **Security & Authorization**:
  - `AuthFilter` implementing Role-Based Access Control (RBAC) across `BUYER`, `SELLER`, and `ADMIN` endpoints.
  - `CsrfFilter` providing state-changing token validation and standard security headers.
  - BCrypt password hashing (cost factor 12) via jBCrypt.
  - Session fixation protection on authentication and complete invalidation on logout.
- **Buyer Features**:
  - Universal product search with multi-faceted filtering (category, price range, stock availability, star ratings) and sorting.
  - Interactive shopping cart with server-authoritative calculations and asynchronous quantity steppers.
  - Checkout workflow with delivery address collection and simulated escrow mock payment.
  - Itemized order history, delivery status progress badges, and order cancellation.
  - Verified buyer review and rating submission system.
- **Seller Operations Hub**:
  - 4 real-time KPI metrics cards (Active Listings, Gross Merchant Sales, Total Orders, Low Stock Alerts).
  - Catalog inventory table with stock indicators and product deletion.
  - Add Creation modal dialog.
  - Customer order fulfillment tracker with inline dispatch status updater.
- **Admin Console**:
  - Marketplace-wide KPI cards and GMV aggregation.
  - User accounts governance table.
  - Platform-wide order moderation and status override controls.
- **Classical Premium UI Design System**:
  - Deep heritage navy, restrained warm gold, and clean canvas neutrals with classical typography.
  - Modal dialogs, toast notifications, confirmation prompts, and empty states.
  - Fully responsive layout across mobile, tablet, laptop, and desktop viewports.
- **Automated Test Suite**:
  - 148 automated tests passing across DAOs, Services, Controllers, Security Filters, and Embedded Tomcat HTTP E2E tests.
  - GitHub Actions CI workflow executing `mvn -B clean verify`.

---

## [0.1.0] - 2026-07-27 (Sprint 0 Baseline)
### Added
- Initial project architecture and Maven skeleton setup.
- Layered MVC package structure (controller, service, dao, model, dto, filter, listener, util, exception).
- Database DDL schema (`db/schema.sql`) and seed data (`db/seed.sql`).
- Error views without stack trace leakage.
