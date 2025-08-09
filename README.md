# ProjectMaster - Residential Construction Management System

A comprehensive Spring Boot application for managing residential construction projects, built as a modular monolith with PostgreSQL database.

## Features

- **User Management**: Multi-role support (Admin, Project Manager, Tradie, Customer)
- **Company Management**: Multi-tenant company structure
- **Flexible Workflow Engine**: Configurable construction workflows
- **Project Management**: Complete project lifecycle management
- **Task Management**: Detailed task tracking and assignment
- **Document Management**: File upload and document storage
- **Invoicing & Billing**: Financial management capabilities
- **Real-time Notifications**: Multi-channel notification system

## Architecture

This application follows a **modular monolith** architecture with clear domain boundaries:

- **Common Module**: Shared utilities, base entities, and common DTOs
- **User Module**: User and company management
- **Project Module**: Project lifecycle management
- **Workflow Module**: Flexible workflow engine
- **Task Module**: Task management and tracking
- **Document Module**: File and document management
- **Invoice Module**: Billing and financial management
- **Notification Module**: Multi-channel notifications

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.3**
- **PostgreSQL** (with Flyway migrations)
- **Spring Data JPA**
- **Spring Security** (JWT-based authentication)
- **Maven** (build tool)
- **Lombok** (code generation)
- **MapStruct** (object mapping)

## Prerequisites

- Java 21 or higher
- PostgreSQL 14+ 
- Maven 3.6+

## Database Setup

1. Install PostgreSQL and create a database:
```sql
CREATE DATABASE projectmaster_db;
CREATE USER projectmaster_user WITH PASSWORD 'projectmaster_password';
GRANT ALL PRIVILEGES ON DATABASE projectmaster_db TO projectmaster_user;
```

2. Update database configuration in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/projectmaster_db
spring.datasource.username=projectmaster_user
spring.datasource.password=projectmaster_password
```

## Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api/v1`

## Database Migrations

The application uses Flyway for database migrations. Migrations are automatically applied on startup:

- `V1__Create_initial_schema.sql`: Creates all tables and indexes
- `V2__Insert_sample_data.sql`: Inserts sample data for testing

## API Endpoints

### Company Management
- `GET /api/v1/companies` - List all active companies
- `POST /api/v1/companies` - Create a new company
- `GET /api/v1/companies/{id}` - Get company by ID
- `PUT /api/v1/companies/{id}` - Update company
- `POST /api/v1/companies/{id}/activate` - Activate company
- `POST /api/v1/companies/{id}/deactivate` - Deactivate company

### User Management
- `GET /api/v1/users` - List users
- `POST /api/v1/users` - Create a new user
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user
- `GET /api/v1/users/company/{companyId}` - Get users by company
- `POST /api/v1/users/{id}/activate` - Activate user
- `POST /api/v1/users/{id}/deactivate` - Deactivate user

## Sample Data

The application includes sample data:

### Company
- **Name**: ABC Construction Company
- **Email**: info@abcconstruction.com

### Users
- **Admin**: admin@abcconstruction.com
- **Project Manager**: pm@abcconstruction.com  
- **Tradie**: tradie@abcconstruction.com

### Customer
- **Name**: Robert Smith
- **Email**: robert.smith@email.com

### Workflow Template
- **Standard Residential Build** with stages:
  1. Site Preparation (Site Survey, Permits, Site Clearing)
  2. Foundation (Excavation, Footings, Foundation Walls)
  3. Framing (Floor, Wall, Roof Framing)

## Testing

Run the tests with:
```bash
mvn test
```

### Manual API Testing

You can test the API endpoints using curl or Postman:

1. **Create a Company**:
```bash
curl -X POST http://localhost:8080/api/v1/companies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Construction Co",
    "address": "123 Test St",
    "phone": "+1-555-0123",
    "email": "test@construction.com"
  }'
```

2. **List Companies**:
```bash
curl http://localhost:8080/api/v1/companies
```

3. **Create a User**:
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": "COMPANY_UUID_HERE",
    "email": "newuser@test.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PROJECT_MANAGER",
    "password": "password123"
  }'
```

## Development

### Project Structure
```
src/main/java/com/projectmaster/app/
├── common/                 # Shared utilities and base classes
│   ├── entity/            # Base entities
│   ├── dto/               # Common DTOs
│   ├── exception/         # Exception handling
│   └── enums/             # Enums
├── config/                # Configuration classes
├── user/                  # User and company management
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── dto/
└── [other modules...]
```

### Adding New Features

1. Create entities in the appropriate module
2. Add repository interfaces
3. Implement service layer with business logic
4. Create REST controllers
5. Add comprehensive tests
6. Update database migrations if needed

## Documentation

- [Architecture Design](ARCHITECTURE.md) - Detailed system architecture
- [Database Schema](DATABASE_SCHEMA.md) - Complete database design
- [API Specification](API_SPECIFICATION.md) - Full API documentation
- [Workflow Engine Design](WORKFLOW_ENGINE_DESIGN.md) - Workflow system details
- [Testing Strategy](TESTING_STRATEGY.md) - Testing approach and guidelines
- [Implementation Roadmap](IMPLEMENTATION_ROADMAP.md) - Development plan

## Contributing

1. Follow the existing code structure and patterns
2. Write comprehensive tests for new features
3. Update documentation as needed
4. Follow the implementation roadmap for new modules

## License

This project is licensed under the MIT License.