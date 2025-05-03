#!/bin/bash

# Run all tests for d-AI-logi project
echo "Running d-AI-logi tests..."

# Frontend tests
echo "===== FRONTEND TESTS ====="
cd ui || exit
echo "Installing dependencies..."
npm install

echo "Running unit tests..."
npm run test

echo "Running E2E tests..."
npm run test:e2e

cd ..

# Backend tests
echo "===== BACKEND TESTS ====="
cd be/dailogi-server || exit
echo "Running backend tests..."
mvn test

echo "Generating JaCoCo report..."
mvn jacoco:report

cd ../..

echo "===== ALL TESTS COMPLETED ======" 