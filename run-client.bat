@echo off
echo Starting Dictionary Client...
echo Make sure server is running first.
echo.
java -cp "target/classes;lib/mysql-connector-java-8.0.33.jar" com.dictionary.client.DictionaryClientGUI
pause
