# Technical Context

<!--
Technologies used.
Development setup.
Technical constraints.
Dependencies.

This file should contain:
- Technology stack
- Development environment setup
- Build and deployment process
- Dependencies and versions
- Infrastructure requirements
- Development tools
- Testing approach
-->

## Technology Stack

### Backend Technologies

**Core Framework:**

- **Java 21+** - Modern Java with records, pattern matching, sealed classes
- **Spring Boot 4.x** - Main application framework
- **Spring Modulith** - Modular monolithic architecture with strict module boundaries
- **Spring Data JPA** - Data persistence layer with Hibernate
- **Spring Security** - Authentication and authorization
- **Spring Web MVC** - REST API implementation

**Database:**

- **PostgreSQL 15+** - Primary relational database
- ACID compliance for financial transactions
- JSON/JSONB support for flexible data structures
- Full-text search capabilities

**Security:**

- **Spring Security OAuth2** - OAuth2 integration
- **JWT (JSON Web Tokens)** - Stateless authentication
- **Google Sign-In** - OAuth2 provider integration
- **BCrypt** - Password hashing algorithm

**Data Mapping:**

- **MapStruct** - Compile-time type-safe mapping between DTOs and domain objects
- Three-layer mapping: Web DTO ↔ Command/Query ↔ Domain ↔ JPA Entity
- **Time Type Conventions:**
    - `Instant` for audit fields (`createdAt`, `updatedAt`) - stored in UTC
    - `LocalDateTime` for business time (`startedAt`, `expectedReturnAt`) - timezone-agnostic
    - `LocalDate` for date-only fields (`birthDate`, `validFrom`)
    - Shared `InstantMapper` provides automatic conversion between `Instant` and `LocalDateTime`

**Reporting & Export:**

- **Apache POI** - Excel file generation and export
- **iText/OpenPDF** - PDF document generation
- CSV export support

**Build & Dependency Management:**

- **Gradle 8.x** - Build automation and dependency management
- **Gradle Groovy DSL** - Build script configuration
- Multi-module project structure

**Testing:**

- **JUnit 5+** - Unit testing framework
- **AssertJ** - Fluent assertions library
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **Testcontainers** - Database integration tests with PostgreSQL containers
- **Awaitility** - Async testing support
- **Spring Modulith Test** - Module structure validation

**CI/CD:**

- **GitHub Actions** - Continuous integration and deployment
- **Docker** - Containerization
- **Docker Compose** - Local development environment orchestration

### Frontend Technologies (Not in Primary Scope)

**Mobile Web:**

- **Web NFC API** - NFC tag reading via Android Chrome
- **QR Code Scanner** - Camera-based equipment identification fallback
- Mobile-responsive web interface

### Infrastructure

**Containerization:**

- **Docker** - Application containerization
- **Docker Compose** - Multi-container orchestration for local development

**Database:**

- **PostgreSQL** - Running in Docker container for development
- Standard PostgreSQL instance for production (no clustering required initially)

**Deployment:**

- **Fat JAR** - Single executable JAR deployment (Spring Boot)
- **Java 21+ Runtime** - JVM required on target servers
- Minimal infrastructure requirements (single server deployment)

## Development Setup

### Prerequisites

**Required Software:**

- **JDK 21 or higher** (OpenJDK recommended)
- **Gradle 8.x** (or use included Gradle wrapper)
- **Docker Desktop** (for local PostgreSQL)
- **Git** - Version control
- **IDE**: IntelliJ IDEA Ultimate (recommended) or Eclipse with Spring Tools

### Local Environment Setup

**1. Clone Repository:**

```bash
git clone https://github.com/JenkaBY/bike-rental.git
cd bikerent
```

**2. Start Local Infrastructure:**

```bash
# Start PostgreSQL database
cd docker
docker-compose up -d

# Verify database is running
docker-compose ps
```

**Docker Compose Configuration** (`docker/docker-compose.yaml`):

**3. Configure Application:**

Create `service/src/main/resources/application-local.properties`. Populate with environment variables are being used
in main `application.yaml` file. For secrets, use `nocommit.properties` file:

```properties

# application-local.properties
DATASOURCE_URL=jdbc:postgresql://localhost:5432/bikerental
----
# nocommit.properties
DATASOURCE_USER=example_user
DATASOURCE_SECRET=verystrongsecret
```

**4. Build Project:**

```bash
# Using Gradle wrapper (recommended)
./gradlew clean build

# Windows
gradlew.bat clean build
```

**5. Run Application:**

```bash
# Run with local profile
./gradlew :service:bootRun --args='--spring.profiles.active=local'

# Or run the built JAR
java -jar service/build/libs/service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

**6. Verify Installation:**

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

## Build Process

### Gradle Project Structure

```
bikerent/
├── build.gradle                 # Root build configuration
├── settings.gradle              # Multi-module project settings
├── gradle.properties            # Project properties
├── gradlew                      # Gradle wrapper script (Unix)
├── gradlew.bat                  # Gradle wrapper script (Windows)
├── service/                     # Main application module
│   └── build.gradle
├── component-test/              # Component test module
│   └── build.gradle
└── gradle/
    └── libs.versions.toml       # Centralized dependency versions
```

### Build Commands

**Clean Build:**

```bash
./gradlew clean build
```

**Build Without Tests:**

```bash
./gradlew clean build -x test
```

**Run Tests Only:**

```bash
# All tests
./gradlew test

# Specific module
./gradlew :service:test
./gradlew :component-test:test -Dspring.profiles.active=test,docker

# Specific test class
./gradlew :service:test -Dspring.profiles.active=test --tests CustomerServiceTest
```

**Create Fat JAR:**

```bash
./gradlew :service:bootJar

# Output: service/build/libs/service-0.0.1-SNAPSHOT.jar
```

**Run Application:**

```bash
./gradlew :service:bootRun
```

**Check Code Quality:**

```bash
# Run checkstyle (if configured)
./gradlew checkstyleMain checkstyleTest

# Run SpotBugs (if configured)
./gradlew spotbugsMain
```

**Generate Module Documentation:**

```bash
# Spring Modulith documentation
./gradlew :service:test --tests ModularityTests
# Output: build/spring-modulith-docs/
```

### Build Optimization

**Gradle Daemon:**

```bash
# Already enabled by default in gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

**Incremental Compilation:**

- Enabled by default in modern Gradle

**Build Scan:**

```bash
./gradlew build --scan
```

## Dependencies

### Core Dependencies (from `gradle/libs.versions.toml`)

### Dependency Management

**Version Catalog Approach:**

- Centralized in `gradle/libs.versions.toml`
- Type-safe accessors in build scripts
- Consistent versions across modules

### Key Version Constraints

- **Java**: 21+ (LTS version)
- **Spring Boot**: 4.x.x (latest stable)
- **Spring Modulith**: 1.1.x (stable release)
- **PostgreSQL**: 15+ (production)
- **Testcontainers**: 2.+ (latest stable)

## Infrastructure

### Development Infrastructure

**Local Setup:**

- Docker Desktop with 4GB+ RAM allocated
- PostgreSQL 15 container
- Application runs on host (port 8080)

**Network:**

```
localhost:8080  → Spring Boot Application
localhost:5432  → PostgreSQL Database (Docker)
```

### Monitoring & Logging

**Spring Boot Actuator:**

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

**Logging:**

```yaml
logging:
  level:
    root: INFO
    com.github.jenkaby.bikerental: INFO
  file:
    name: logs/bikerent.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Testing Strategy

### Test-Driven Development (TDD)

**Approach:**

1. Write test first (Red)
2. Implement minimum code to pass (Green)
3. Refactor (Refactor)
4. Repeat

**Test Pyramid:**

```
       ┌─────────────┐
       │  Component  │  ← Few (End-to-end, positive scenarios)
       │    Tests    │
       ├─────────────┤
       │     Unit    │  ← Many (Business logic, edge cases)
       │    Tests    │
       └─────────────┘
```

### Test Types

**1. Unit Tests** (`service/src/test/java`)

- **Purpose**: Test business logic in isolation
- **Scope**: Single class, mocked dependencies
- **Framework**: JUnit 5 + Mockito + AssertJ
- **Location**: Same package as source class

**Example:**

```java
@ExtendWith(MockitoExtension.class)
class RentalCostCalculatorTest {
    
    @Mock
    private BusinessRulesConfig config;
    
    @InjectMocks
    private RentalCostCalculator calculator;
    
    @Test
    void shouldApplyForgivenessRuleForDelayUnder7Minutes() {
        // Given
        when(config.getForgivenessThresholdMinutes()).thenReturn(7);
        
        // When
        CostBreakdown result = calculator.calculate(rental, returnTime);
        
        // Then
        assertThat(result.overtimeCost()).isEqualTo(Money.ZERO);
        assertThat(result.forgivenessApplied()).isTrue();
    }
}
```

**2. Component Tests** (`component-test/src/test/java`)

- **Purpose**: Test REST endpoints with real HTTP
- **Scope**: Components interaction, database integration
- **Framework**: Spring Boot Test + Cucumber
- **Focus**: Positive scenarios only (validation tested separately)
- **Profile**: Must use `test` profile

**3. WebMvc Tests** (`service/src/test/java`)

- **Purpose**: Test request validation and error handling
- **Scope**: Controller layer only
- **Framework**: Spring WebMvcTest
- **Focus**: Validation rules, error responses
- **Profile**: Must use `test` profile

**Example:**

```java

@WebMvcTest(RentalCommandController.class)
class RentalCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateRentalUseCase createRentalUseCase;

    @Test
    void shouldRejectRequestWhenCustomerIdIsNull() throws Exception {
        // Given
        String request = """
                {
                    "equipmentId": "123e4567-e89b-12d3-a456-426614174000"
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'customerId')]").exists());
    }
}
```

**4. Spring Modulith Tests**

- **Purpose**: Validate module boundaries and structure
- **Framework**: Spring Modulith Test

**Example:**

```java
class ModularityTests {
    
    ApplicationModules modules = ApplicationModules.of(BikeRentalApplication.class);
    
    @Test
    void shouldVerifyModuleStructure() {
        modules.verify();
    }
    
    @Test
    void shouldNotHaveCyclicDependencies() {
        modules.verify().assertNoCycles();
    }
    
    @Test
    void shouldRespectModuleBoundaries() {
        modules.verify().assertModuleBoundaries();
    }
}
```

**5. Unit Tests** (`service/src/test/java`)

- **Purpose**: Test request validation and error handling
- **Scope**: Service and business logic layers, mappers
- **Framework**: Junit 5+, Mockito, AssertJ
- **Focus**: All scenario including edge cases

### Async Testing

**Awaitility for Event-Driven Tests:**

```java
@Test
void shouldUpdateEquipmentStatusAfterRentalCompleted() {
    // Given
    returnEquipmentUseCase.execute(rentalId);
    
    // When/Then
    await().atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> {
            Equipment equipment = equipmentRepository.findById(equipmentId).orElseThrow();
            assertThat(equipment.getStatus()).isEqualTo(EquipmentStatus.AVAILABLE);
        });
}
```

### Test Execution

**Run All Tests:**

```bash
./gradlew test '-Dspring.profiles.active=test'
```

**Run Specific Test Class:**

```bash
./gradlew test --tests  '-Dspring.profiles.active=test'
```

### Testing Best Practices

1. **Test Naming:** `shouldDoSomethingWhenCondition()`
2. **AAA Pattern:** Arrange, Act, Assert (Given, When, Then)
3. **One Assertion Per Test:** Focus on single behavior
4. **Use AssertJ:** Fluent, readable assertions
5. **Mock External Dependencies:** Database, HTTP clients, etc.
6. **Test Edge Cases:** Null, empty, boundary values
7. **Keep Tests Fast:** Unit tests < 100ms, integration tests < 5s
8. **Independent Tests:** No test should depend on another
9. **Clean Up:** Reset state after each test
10. **Test Profile:** Always use `test` profile for component tests

### Code Coverage Goals

- **Unit Tests**: 80%+ coverage for service and domain layers
- **Component Tests**: Cover all positive happy paths
- **Critical Business Logic**: 100% coverage (cost calculations, state transitions)

---

This technical context provides complete guidance for developers to set up, build, test, and deploy the BikeRental
system using modern Java and Spring Boot practices.
