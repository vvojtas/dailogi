#!/bin/bash

# Run E2E tests for d-AI-logi project with local backend
echo "Running d-AI-logi E2E tests with local backend..."

# Start backend with local profile (H2 database)
echo "===== STARTING LOCAL BACKEND ====="
cd be/dailogi-server || exit
mvn spring-boot:run -Dspring-boot.run.profiles=local,e2e-test > /dev/null 2>&1 &
BACKEND_PID=$!

# Wait for backend to start by checking the health endpoint
echo "Waiting for backend to start..."
MAX_RETRIES=30
RETRY_COUNT=0
RETRY_DELAY=2

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    RETRY_COUNT=$((RETRY_COUNT+1))
    echo "Checking if backend is healthy (attempt $RETRY_COUNT of $MAX_RETRIES)..."
    
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    echo "HTTP Status: $HTTP_STATUS"
    
    if [ "$HTTP_STATUS" = "200" ]; then
        echo "Backend is healthy and ready!"
        break
    fi
    
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo "Backend failed to start within the expected time."
        kill $BACKEND_PID
        exit 1
    fi
    
    sleep $RETRY_DELAY
done

# Run E2E tests on frontend with Playwright
echo "===== RUNNING FRONTEND E2E TESTS ====="
cd ../../ui || exit
npm run test:e2e
E2E_EXIT_CODE=$?

# Cleanup - Kill the backend process
echo "===== CLEANING UP ====="
kill $BACKEND_PID

cd ..

echo "===== E2E TESTS COMPLETED ====="
exit $E2E_EXIT_CODE 