@echo off
REM Run script for Lab 6 - Multi-threaded Warehouse Management System

echo Starting Warehouse Management System (Lab 6)...
echo.

java -cp target\classes org.example.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error: Could not run the application.
    echo Make sure to compile first using compile.bat
    echo Or ensure Java is installed and in PATH.
)

pause
