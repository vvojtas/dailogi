@echo off
SETLOCAL EnableDelayedExpansion

REM Clean up previous backend log if it exists
del scripts\logs\backend.log > nul 2>&1

echo Running d-AI-logi E2E tests with local backend...

REM Start backend server with local profile (H2 database) in a new window
echo ===== STARTING LOCAL BACKEND =====
cd be\dailogi-server
start "Dailogi Backend (Local+E2E)" cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=local,e2e-test > ..\..\scripts\logs\backend.log 2>&1"
set BACKEND_STARTED=true

REM Wait for backend to start by checking the health endpoint
echo Waiting for backend to start...
set MAX_RETRIES=30
set RETRY_COUNT=0
set RETRY_DELAY=2

:WAIT_LOOP
set /a RETRY_COUNT+=1
echo Checking if backend is healthy (attempt !RETRY_COUNT! of !MAX_RETRIES!)...
for /f "delims=" %%i in ('curl -k -s -o NUL -w "%%{http_code}" https://localhost/actuator/health') do set HTTP_STATUS=%%i

if "!HTTP_STATUS!"=="200" (
    echo Backend is healthy and ready!
    goto :CONTINUE
)

if !RETRY_COUNT! geq !MAX_RETRIES! (
    echo Backend failed to start within the expected time.
    goto :ERROR
)

timeout /t !RETRY_DELAY! /nobreak > nul
goto :WAIT_LOOP

:CONTINUE
REM Run E2E tests on frontend with Playwright
echo ===== RUNNING FRONTEND E2E TESTS =====
cd ..\..\ui
echo Running E2E tests...
call npm run test:e2e
set E2E_EXIT_CODE=!ERRORLEVEL!

cd ..

REM Ensure cleanup happens before exiting
call :CLEANUP

echo ===== E2E TESTS COMPLETED =====
exit /b !E2E_EXIT_CODE!

:ERROR
echo ===== ERROR: E2E TESTS FAILED =====
call :CLEANUP
exit /b 1

:CLEANUP
echo ===== CLEANING UP BACKEND PROCESS =====
REM Kill all Java processes running Spring Boot via WMIC
for /f "tokens=1" %%p in ('wmic process where "name='java.exe' and commandline like '%%spring-boot:run%%'" get processid ^| findstr [0-9]') do (
    echo Killing Spring Boot Java process with PID: %%p
    taskkill /F /PID %%p > nul 2>&1
)

REM Find and kill any process listening on port 443 (fallback)
for /F "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:"TCP.*:443 " ^| findstr "LISTENING"') do (
    echo Killing process listening on port 443: %%P
    taskkill /F /PID %%P > nul 2>&1
)

exit /b 0

ENDLOCAL 