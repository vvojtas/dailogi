# d‑AI‑logi

test
Welcome to **d‑AI‑logi**, an interactive web application that combines engaging AI-driven dialogues with an innovative flashcard-based learning system. This project leverages state-of-the-art AI models to generate flashcards from user-supplied text, allowing users to learn more efficiently while interacting with dynamic AI personas.

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)
- [Project Scope](#project-scope)
- [Testing](#testing)
- [Project Status](#project-status)
- [License](#license)

## Project Description

d-ai-logi is an interactive web application that enables users to create and observe dialogues between various AI personas. The platform allows users to:

- Choose from predefined characters or design their own
- Set up scenes with 2-3 characters
- Define conversation topics
- Observe AI-generated dialogues between the characters

The application aims to provide an engaging and entertaining way to explore AI interactions in a controlled, user-defined environment.

## Tech Stack

### Frontend
- **Astro 5**: Fast, optimized static site generation with minimal JavaScript
- **React 19**: For interactive components
- **TypeScript 5**: Static typing and improved IDE support
- **Tailwind 4**: Utility-first CSS framework
- **Shadcn/ui**: Accessible component library

### Backend
- **Java 21**: Latest LTS version
- **Spring Boot**: Application framework
  - Spring Data: Database integration
  - Spring Security: Authentication and authorization
  - Spring Web: REST API
- **JUnit 5 & Mockito**: Testing frameworks
- **Lombok**: Reduces boilerplate code

### Database
- **PostgreSQL**: Relational database

### Testing
- **Frontend Testing**:
  - Vitest: Unit testing for JS/TS
  - React Testing Library: Component testing
  - Playwright: End-to-end testing
- **Backend Testing**:
  - JUnit 5: Java testing framework
  - Mockito: Mocking dependencies
  - Spring Boot Test: Integration testing
  - H2: Database testing
  - MockMvc/RestAssured: API testing
- **API Testing**:
  - Swagger: Manual API tests

### AI Integration  
- LLM models via API (Openrouter.ai or similar)

### CI/CD & Hosting
- **GitHub Actions**: CI/CD pipelines
- **Docker**: Application containerization

## Getting Started Locally

### Prerequisites
- Node.js 22.14.0 (use nvm for version management)
- Java 21
- PostgreSQL
- Maven

### Frontend Setup
1. Navigate to the `ui` directory:
   ```bash
   cd ui
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```
   This starts the Astro development server for the frontend.

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd be/dailogi-server
   ```

2. Build the application:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   This starts the backend server on the default port.

## Available Scripts

### Frontend Scripts
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run astro` - Run Astro CLI commands
- `npm run lint` - Run ESLint
- `npm run lint:fix` - Run ESLint with auto-fix
- `npm run format` - Format code with Prettier

### Backend Scripts
- `mvn clean install` - Clean and install dependencies
- `mvn spring-boot:run` - Run the application
- `mvn test` - Run tests

## Project Scope

### Core Features
- Interactive web application with Astro and React
- Character creation and customization
- Scene setup with 2-3 characters
- Dialogue generation based on defined topics
- User authentication and account management
- Secure data storage and retrieval

### Project Structure
- `./ui/src` - UI source code
- `./ui/src/layouts` - Astro layouts
- `./ui/src/pages` - Astro pages
- `./ui/src/components` - UI components
- `./ui/src/lib` - Services and helpers
- `./be/dailogi-server/src` - Backend source code
- `./public` - Public assets

## Testing

The application uses a comprehensive testing strategy:

### Frontend Testing
- **Unit Tests**: Using Vitest and React Testing Library to test individual components, hooks, and helper functions
- **Component Integration Tests**: Verifying interactions between related React components
- **End-to-End Tests**: Using Playwright to simulate complete user flows in a browser

### Backend Testing
- **Unit Tests**: Testing individual service classes, controllers, and helper classes using JUnit 5 and Mockito
- **Integration Tests**: Verifying interactions between application layers using Spring Boot Test, H2 for database testing
- **API Tests**: Testing API endpoints using MockMvc/RestAssured and Swagger

### Running Tests

```bash
# Run all tests (both frontend and backend)
./scripts/run-tests.sh  # Linux/Mac
scripts/run-tests.bat   # Windows

# Frontend tests only
cd ui
npm run test          # Run unit tests
npm run test:e2e      # Run E2E tests

# Backend tests only
cd be/dailogi-server
mvn test
```

For more detailed testing documentation, see [TESTING.md](TESTING.md).

## Project Status

The project is currently in early development (version 0.0.1).

## License

License is still under consideration - code comes with no guarantees.
