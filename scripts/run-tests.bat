@echo off
SETLOCAL

echo Running d-AI-logi tests...

REM Frontend tests
echo ===== FRONTEND TESTS =====
cd ui
echo Installing dependencies...
call npm install

echo Running unit tests...
call npm run test

echo Running E2E tests...
call npm run test:e2e

cd ..

REM Backend tests
echo ===== BACKEND TESTS =====
cd be\dailogi-server
echo Running backend tests...
call mvn test

echo Generating JaCoCo report...
call mvn jacoco:report

cd ..\..

echo ===== ALL TESTS COMPLETED =====

ENDLOCAL 