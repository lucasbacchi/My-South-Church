# My South Church — Backend

A Spring Boot backend for managing Google Workspace users, groups, and Drive access for a multi-ministry church organization. Provides a centralized interface for church IT admins to manage access, visibility, and organization of Workspace resources.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Project Status](#project-status)

---

## Tech Stack

| Layer          | Technology                              |
|----------------|-----------------------------------------|
| Backend        | Java 17, Spring Boot, Maven             |
| Database       | MySQL (Aiven)                           |
| ORM            | JPA / Hibernate                         |
| Auth           | Firebase Authentication (JWT)           |
| Google APIs    | Directory API, Drive API, Groups Settings API, Cloud Identity API |
| Infrastructure | Google Cloud Run                        |
| Containers     | Jib (no Dockerfile)                     |
| CI/CD          | GitHub Actions                          |
| Frontend       | React + TypeScript (separate repo/module) |

---

## Architecture

### Service Pattern
All business logic follows a strict **Command / Query** pattern:
```java
// Mutations
Command<Input, Output>

// Reads
Query<Input, Output>
```

Each use case has its own service class (e.g. `CreatePersonService`, `GetGroupByIdService`). Controllers are thin — they delegate directly to services.

### Data Strategy
| Resource       | Storage         |
|----------------|-----------------|
| People         | MySQL (Aiven)   |
| Roles          | MySQL (Aiven)   |
| Group → Drive mappings | MySQL (Aiven) |
| Groups         | Google Directory API (live) |
| Group members  | Google Directory API (live) |
| Drive hierarchy | Caffeine cache (rebuilt daily) |

---

## Features

### People
Full CRUD for church members stored in MySQL:
- Firebase UID linkage
- Role assignment with Firebase custom claims sync
- Google account verification on create/update
- Caching (`peopleAllCache`, `personByIdCache`, `personByEmailCache`, `personByFirebaseCache`)

**Login flow:**
1. Lookup by Firebase UID → fallback to email
2. Update `lastLogin` timestamp
3. Detect Google sign-in provider → auto-set `googleAccountVerified`

### Groups
Groups are **not stored in the database** — all group data is fetched live from the Google Directory API:
- List all groups in domain
- Get group by ID
- Get group members
- Get group settings
- Create / delete groups
- Add / remove members (single and bulk)
- Group aliases (list, insert, delete)
- Update group settings

### Drive
Determines a user's **entry points** into Google Drive — the highest-level folders or shared drives they have access to, without showing redundant nested paths:

- `DriveLookupService` — fetches org folders, runs parent-child elimination to find roots
- `DriveHierarchyCacheService` — caches the full org folder hierarchy daily
- `DriveHierarchySyncJob` — cron job that refreshes the cache at 2am
- `DriveMappingSyncService` — scans shared drive permissions and populates the `group_drive_mappings` table
- `GetSharedDrivesService` — resolves a user's groups → their mapped resource IDs → entry points from cache

---

## Getting Started

### Prerequisites
- Java 17
- Maven
- MySQL (or use Aiven connection)
- Firebase project
- Google Cloud service account with domain-wide delegation

### Local Setup

1. **Clone the repo**
```bash
git clone <repo-url>
cd backend
```

2. **Create a `.env` file** in the `backend/` directory:
```env
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
SPRING_SECURITY_USER_NAME=your-username
SPRING_SECURITY_USER_PASSWORD=your-password
GOOGLE_ROOT_GROUP_EMAIL=root-group@yourdomain.com
GOOGLE_WORKSPACE_DOMAIN=yourdomain.com
DB_URL=jdbc:mysql://your-aiven-host:port/dbname?ssl-mode=REQUIRED
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password
GOOGLE_DRIVE_TEST_FILE_ID=your-test-file-id
```

3. **Place credentials** in `src/main/resources/`:
   - `service-account.json` — Google service account key
   - `truststore.jks` — Aiven SSL truststore

   > ⚠️ Both files are gitignored and should **never** be committed.

4. **Run the application**
```bash
mvn spring-boot:run
```

---

## Configuration

### Authentication Flow
1. Client authenticates via Firebase → receives JWT
2. Spring Security validates JWT via Firebase issuer URI
3. `UserAccessValidator` checks:
   - Email is verified
   - User belongs to configured root Google group
   - Extracts roles from Firebase custom claims
4. Request is allowed or denied

### Google Credentials
The service account requires the following OAuth scopes:
- `https://www.googleapis.com/auth/admin.directory.group`
- `https://www.googleapis.com/auth/admin.directory.group.member`
- `https://www.googleapis.com/auth/admin.directory.user.readonly`
- `https://www.googleapis.com/auth/apps.groups.settings`
- `https://www.googleapis.com/auth/cloud-identity.groups.readonly`
- `https://www.googleapis.com/auth/drive`

Domain-wide delegation must be enabled in the Google Admin Console.

### Caching
Caffeine is used for all caches. Cache names:

| Cache Name             | TTL      | Purpose                        |
|------------------------|----------|--------------------------------|
| `peopleAllCache`       | 25 hours | All people list                |
| `personByIdCache`      | 25 hours | Person by UUID                 |
| `personByEmailCache`   | 25 hours | Person by email                |
| `personByFirebaseCache`| 25 hours | Person by Firebase UID         |
| `groupMembershipCache` | 20 min   | Root group membership check    |
| `driveHierarchy`       | 25 hours | Full org Drive folder hierarchy|

---

## Testing

### Setup
- JUnit 5 + Mockito
- H2 in-memory database for test context
- Google API clients mocked via `TestGoogleConfig`
- `@ActiveProfiles("test")` disables Firebase and Google credential initialization

### Running Tests
```bash
mvn test
```

### Test Conventions

**Creating `Person` objects in tests:**
```java
// When you need real field values — use reflection helper
private Person buildPerson(String firstName, String lastName, String email) {
    java.lang.reflect.Constructor<Person> constructor = Person.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    Person person = constructor.newInstance();
    person.setFirstName(firstName);
    // ...
    return person;
}

// When you just need a non-null Person — use mock
Person person = mock(Person.class);
```

**Mocking Google API chains:**
```java
@Mock private Directory directory;
@Mock private Directory.Members members;
@Mock private Directory.Members.Insert insert;

when(directory.members()).thenReturn(members);
when(members.insert(any(), any())).thenReturn(insert);
when(insert.execute()).thenReturn(new Member());
```

### Coverage Areas
- Happy paths
- Validation failures
- External service behavior (Firebase, Google APIs)
- Edge cases (nulls, blank inputs, fallbacks)

---

## CI/CD

### CI — runs on every push/PR to `dev`
```
Checkout → JDK 17 → mvn test
```

### CD — runs on every push to `main`
```
Checkout → JDK 17 → Authenticate GCP → Jib build → Push to GCR → Deploy to Cloud Run
```

### Required GitHub Secret
| Secret | Description |
|--------|-------------|
| `GOOGLE_CREDENTIALS_JSON` | Full contents of the service account JSON file |

### Deployment
The app is deployed to **Google Cloud Run** in `us-east1`. All production secrets are managed via **Google Secret Manager** and injected at runtime via `--set-secrets`.

---

## Project Status

| Area | Status |
|------|--------|
| Person services | ✅ Complete + tested |
| Group services | ✅ Complete + tested |
| Drive entry point detection | 🚧 In progress |
| Drive hierarchy caching | ✅ Implemented |
| Drive mapping sync | ✅ Implemented |
| CI pipeline | ✅ Working |
| CD pipeline | ✅ Configured |

### Known Limitations
- Drive lookup performance is a bottleneck — Drive API pagination is sequential and cannot be parallelized
- Drive hierarchy is rebuilt daily via cron — changes within the day are not reflected until the next sync
- Role-based endpoint authorization (`@PreAuthorize`) is partially implemented on people endpoints, not yet on group endpoints

### Next Steps
- Optimize Drive entry point detection with caching strategy
- Complete `@PreAuthorize` coverage on group endpoints
- Expand Drive tree building to support full hierarchy display
- Continue expanding test coverage