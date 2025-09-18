@echo off
echo Building Dictionary Application...
echo.

echo Creating target directory...
if not exist target\classes mkdir target\classes

echo Compiling Java files...
javac -encoding UTF-8 -cp "lib/mysql-connector-java-8.0.33.jar" -d target/classes src/main/java/com/dictionary/*.java src/main/java/com/dictionary/client/*.java src/main/java/com/dictionary/server/*.java src/main/java/com/dictionary/database/*.java src/main/java/com/dictionary/model/*.java

if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo.
    echo To run the application:
    echo 1. Start MySQL server
    echo 2. Run database/schema.sql to create database
    echo 3. Run run-server.bat to start server
    echo 4. Run run-client.bat to start client
) else (
    echo Build failed! Please check the errors above.
)

pause
