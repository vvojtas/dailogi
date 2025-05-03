# Testing Guide for d-AI-logi

This document outlines the testing setup, procedures, and best practices for the d-AI-logi project.

## Table of Contents
- [Overview](#overview)
- [Frontend Testing](#frontend-testing)
  - [Unit Testing with Vitest](#unit-testing-with-vitest)
  - [E2E Testing with Playwright](#e2e-testing-with-playwright)
- [Backend Testing](#backend-testing)
  - [Unit Testing with JUnit](#unit-testing-with-junit)
  - [Integration Testing with Spring Boot Test](#integration-testing-with-spring-boot-test)
  - [Repository Testing with DataJpaTest](#repository-testing-with-datajpatest)
- [Running Tests](#running-tests)
- [Writing Tests](#writing-tests)
- [Test Environments](#test-environments)

## Overview

The d-AI-logi project employs a comprehensive testing strategy across both frontend and backend codebases:

- **Frontend**: Vitest for unit tests and React components, Playwright for E2E tests
- **Backend**: JUnit 5 and Mockito for unit tests, Spring Test for integration tests

## Frontend Testing

### Unit Testing with Vitest

Located in `ui/src/components/**/*.test.tsx` and similar patterns.

These tests verify individual components, hooks, and helper functions in isolation.

Key characteristics:
- Fast execution
- No backend dependencies
- Isolated component rendering
- Mock external dependencies

Examples:
- Testing a button component renders correctly
- Verifying a form validation function works as expected
- Testing a custom hook's state changes

### E2E Testing with Playwright

Located in `ui/e2e/**/*.spec.ts`.

These tests verify complete user flows by simulating real user interactions in a browser.

Key characteristics:
- Tests real UI interactions
- Covers complete flows from UI to backend
- Slower execution than unit tests
- More comprehensive coverage

Examples:
- User registration flow
- Character creation and editing
- Starting and observing a dialogue

## Backend Testing

### Unit Testing with JUnit

Located in `be/dailogi-server/src/test/java/com/github/vvojtas/dailogi_server/service/`.

These tests verify individual service classes, controller methods, and utility classes in isolation.

Key characteristics:
- Fast execution
- Dependencies are mocked
- No database access
- No Spring context loading

### Integration Testing with Spring Boot Test

Located in `be/dailogi-server/src/test/java/com/github/vvojtas/dailogi_server/controller/`.

These tests verify interactions between application layers and Spring components.

Key characteristics:
- Tests API endpoints with MockMvc
- Spring context may be loaded
- Dependencies can be mocked
- Database can be real or in-memory H2

### Repository Testing with DataJpaTest

Located in `be/dailogi-server/src/test/java/com/github/vvojtas/dailogi_server/db/repository/`.

These tests verify JPA repositories and database operations.

Key characteristics:
- Uses H2 in-memory database
- Tests actual SQL queries
- Focuses on data access layer
- Uses @DataJpaTest for Spring test slicing

## Running Tests

### Frontend Tests

```bash
# Navigate to UI directory
cd ui

# Run unit tests
npm run test          # Run all tests once
npm run test:watch    # Run tests in watch mode
npm run test:coverage # Run tests with coverage report

# Run E2E tests
npm run test:e2e      # Run all E2E tests
npm run test:e2e:ui   # Run E2E tests with UI
npm run test:e2e:debug # Run E2E tests in debug mode
```

### Backend Tests

```bash
# Navigate to backend directory
cd be/dailogi-server

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ExampleServiceTest

# Run with coverage report (JaCoCo)
mvn test jacoco:report
```

## Writing Tests

### Frontend Testing Guidelines

1. **Component Tests**:
   - Use React Testing Library's `render` and `screen` methods
   - Test for accessibility using `getByRole` where possible
   - Prefer user-centric testing (what the user sees) over implementation details

2. **E2E Tests**:
   - Use Page Object Model pattern (see `ui/e2e/models/`)
   - Keep tests independent of each other
   - Use test data setup/teardown hooks

### Backend Testing Guidelines

1. **Unit Tests**:
   - Mock all dependencies using Mockito
   - Test edge cases and error conditions
   - Use parameterized tests for testing multiple similar cases

2. **Repository Tests**:
   - Use the `@DataJpaTest` annotation
   - Verify all custom query methods
   - Use the test-specific application-test.yml configuration

3. **Controller Tests**:
   - Use MockMvc for testing endpoints
   - Verify status codes, response formats, and error handling
   - Mock service layer

## Test Environments

1. **Local Development**:
   - Unit tests with mocked dependencies
   - In-memory H2 database for backend
   - Mocked external services

2. **CI Pipeline**:
   - Automated test execution on pull requests
   - Coverage reports
   - All tests must pass before merging

3. **Staging**:
   - Selected E2E tests run against staging environment
   - Manual testing of new features 