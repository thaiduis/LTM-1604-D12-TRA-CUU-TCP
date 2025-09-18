@echo off
chcp 65001 > nul
echo Starting Dictionary Server...
echo Make sure MySQL is running and database is created.
echo.
java -Dfile.encoding=UTF-8 -cp "target/classes;lib/mysql-connector-java-8.0.33.jar" com.dictionary.server.DictionaryServer
pause
