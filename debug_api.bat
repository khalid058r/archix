@echo off
echo ===================================================
echo ARCHIX DEBUGGER - API & HEADERS
echo ===================================================

REM 1. Define Credentials
set API_URL=http://localhost:8081/api
set EMAIL=admin@archix.com
set PASSWORD=admin

REM 2. Login to get Token
echo.
echo [1] Logging in as %EMAIL%...
curl -X POST "%API_URL%/auth/login" ^
     -H "Content-Type: application/json" ^
     -d "{\"email\":\"%EMAIL%\", \"password\":\"%PASSWORD%\"}" > login_response.json 2>nul

echo Login response saved to login_response.json.

REM Extract Token (Requires user to manually copy if no jq, but we just want to see if it works)
echo.
echo PLEASE CHECK login_response.json.
echo.
echo If login failed, the backend is reachable but auth is wrong.
echo If it succeeded, we need the token to continue.
echo.
echo [2] Testing Documents Endpoint (Simulating the 400 error)
echo Sending request WITHOUT X-Organization-ID...
curl -v "%API_URL%/documents?page=0&size=1" 2>nul
echo.
echo.
echo Expected Result: 400 Bad Request (Response body should be empty or error)

echo.
echo [3] Testing with Manual Header (Simulating Success)
echo To test success, use this command with your actual token and org ID:
echo curl -H "Authorization: Bearer YOUR_TOKEN" -H "X-Organization-ID: 1" "%API_URL%/documents?page=0&size=1"
echo.
pause
