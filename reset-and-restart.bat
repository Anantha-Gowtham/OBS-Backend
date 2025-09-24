@echo off
echo ===========================================
echo     OBS Database Reset and Restart
echo ===========================================

echo.
echo Stopping any running Java processes for OBS...
taskkill /F /IM java.exe 2>nul

echo.
echo Connecting to MySQL and resetting database...
mysql -u root -proot obs_banking_system < database-reset.sql

if %ERRORLEVEL% NEQ 0 (
    echo Error: Failed to reset database. Please check:
    echo 1. MySQL server is running
    echo 2. Database 'obs_banking_system' exists
    echo 3. Username/password are correct
    pause
    exit /b 1
)

echo.
echo Database reset successful!
echo.
echo Starting OBS Backend...
mvn spring-boot:run

pause