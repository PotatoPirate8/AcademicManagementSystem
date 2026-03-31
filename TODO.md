# Project TODO Backlog

This backlog is prioritised by implementation importance and overall project impact.

## Importance Ranking Legend

- **P1 (Critical)**: Should be done first; high impact on correctness, security, or architecture.
- **P2 (High)**: Strong value; major quality or maintainability improvements.
- **P3 (Medium)**: Useful enhancements that improve usability and polish.
- **P4 (Low)**: Nice-to-have additions for scope expansion.

## Tags

- `ARCH`: Architecture and code structure
- `SEC`: Security
- `DB`: Database and persistence
- `TEST`: Testing and quality
- `DEVX`: Developer experience and CI/CD
- `UX`: User experience
- `DOC`: Documentation
- `FEATURE`: Domain/business feature expansion

## Ranked TODO List

| Done | Rank | Priority | Tags | Item | Why It Matters | Created At | Completed At |
|------|------|----------|------|------|----------------|------------|--------------|
| [x] | 1 | P1 | `ARCH`, `TEST` | Add a **Service Layer** (`StudentService`, `CourseService`, `EnrollmentService`) between controllers and DAOs | Centralises business rules, reduces controller complexity, and makes logic easier to test | 2026-03-18 14:00 | 2026-03-25 14:20 |
| [x] | 2 | P1 | `SEC`, `FEATURE` | Implement **Role-Based Access Control (RBAC)** for admin/student/lecturer actions | Prevents unauthorized actions and enforces correct role permissions | 2026-03-18 15:00 | 2026-03-25 15:20 |
| [x] | 3 | P1 | `DB`, `ARCH` | Improve database reliability: add **transactions**, introduce **schema migrations** (Flyway/Liquibase), and consider **connection pooling** | Prevents inconsistent data and supports safe schema evolution | 2026-03-18 15:40 | 2026-03-25 16:10 |
| [x] | 4 | P2 | `TEST` | Expand automated tests: integration tests for DAOs, service tests, and edge-case workflow tests | Increases confidence and prevents regressions | 2026-03-18 15:40 | 2026-03-25 16:20 |
| [ ] | 5 | P2 | `DEVX`, `TEST` | Add CI pipeline (GitHub Actions): run `mvn test` + static analysis (Checkstyle/SpotBugs/PMD) | Enforces quality checks on every push/PR | 2026-03-18 15:40 | - |
| [x] | 6 | P2 | `ARCH`, `DEVX` | Centralize error handling and add structured logging (SLF4J + Logback) | Speeds up debugging and makes failures easier to diagnose | 2026-03-18 15:40 | 2026-03-25 17:30 |
| [x] | 7 | P3 | `UX`, `FEATURE` | Improve dashboard usability: search/filter/sort, pagination, and report export (CSV/PDF) | Makes the app more practical for larger datasets | 2026-03-18 15:40 | 2026-03-31 12:00 |
| [ ] | 8 | P3 | `DOC`, `DEVX` | Expand project documentation: architecture diagrams, workflow sequence diagrams, setup/testing guide improvements | Makes onboarding, maintenance, and coursework assessment clearer | 2026-03-18 15:40 | - |
| [ ] | 9 | P4 | `FEATURE` | Add advanced academic capabilities: GPA/transcripts, prerequisites, attendance, lecturer comments | Extends project scope and demonstrates deeper domain modelling | 2026-03-18 15:40 | - |

## Suggested Delivery Order

1. Service Layer + RBAC (foundation)
2. DB reliability (safety)
3. Test expansion + CI + logging (quality)
4. UX improvements + docs (usability)
5. Advanced academic features (scope expansion)
