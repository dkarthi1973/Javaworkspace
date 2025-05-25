@echo off
REM Middleware Ticket Chatbot - Windows Quick Start Script
REM This script helps set up and run the chatbot application on Windows

setlocal enabledelayedexpansion

echo.
echo 🚀 Middleware Ticket Chatbot - Windows Quick Start Setup
echo ========================================================
echo.

REM Function to print colored output (Windows doesn't support colors easily, so we'll use simple text)
echo [INFO] Starting Windows setup process...

REM Check if Java is installed
echo.
echo [INFO] Checking prerequisites...

java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Java not found. Please install Java 17+ from: https://adoptium.net/
    echo [ERROR] Make sure JAVA_HOME is set and Java is in your PATH
    pause
    exit /b 1
) else (
    echo [SUCCESS] Java found ✓
    java -version 2>&1 | findstr "version"
)

REM Check Maven
mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Maven not found. Please install Maven from: https://maven.apache.org/
    echo [ERROR] Make sure MAVEN_HOME is set and Maven is in your PATH
    pause
    exit /b 1
) else (
    echo [SUCCESS] Maven found ✓
    mvn -version 2>&1 | findstr "Apache Maven"
)

REM Check if Ollama is installed
ollama --version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Ollama not found. Please install from: https://ollama.ai
    echo [ERROR] After installation, run: ollama pull llama3
    pause
    exit /b 1
) else (
    echo [SUCCESS] Ollama found ✓
)

REM Check if Ollama service is running by trying to connect
curl -s http://localhost:11434 >nul 2>&1
if !errorlevel! neq 0 (
    echo [WARNING] Ollama service not running. Please start Ollama manually.
    echo [INFO] You can start Ollama by running 'ollama serve' in another command prompt
    echo [INFO] Or simply run 'ollama run llama3' to start both service and model
    set /p continue="Press Enter to continue once Ollama is running, or Ctrl+C to exit..."
) else (
    echo [SUCCESS] Ollama service is running ✓
)

REM Check if llama3 model is available
ollama list | findstr "llama3" >nul 2>&1
if !errorlevel! neq 0 (
    echo [WARNING] Llama3 model not found. Downloading...
    echo [INFO] This may take several minutes depending on your internet connection...
    ollama pull llama3
    if !errorlevel! neq 0 (
        echo [ERROR] Failed to download Llama3 model
        pause
        exit /b 1
    ) else (
        echo [SUCCESS] Llama3 model downloaded ✓
    )
) else (
    echo [SUCCESS] Llama3 model found ✓
)

echo.
echo [SUCCESS] All prerequisites met! ✨

REM Build the application
echo.
echo [INFO] Building the application...
mvn clean compile > build.log 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Build failed. Check build.log for details.
    pause
    exit /b 1
) else (
    echo [SUCCESS] Application compiled successfully ✓
)

REM Ask about running tests
echo.
set /p run_tests="Run tests before starting? (y/N): "
if /i "!run_tests!"=="y" (
    echo [INFO] Running tests...
    mvn test > test.log 2>&1
    if !errorlevel! neq 0 (
        echo [WARNING] Some tests failed. Check test.log for details.
        set /p continue_anyway="Continue anyway? (y/N): "
        if /i not "!continue_anyway!"=="y" (
            exit /b 1
        )
    ) else (
        echo [SUCCESS] All tests passed ✓
    )
)

REM Check if port 8080 is in use
echo.
echo [INFO] Checking if port 8080 is available...
netstat -ano | findstr ":8080" >nul 2>&1
if !errorlevel! equ 0 (
    echo [WARNING] Port 8080 is already in use. Please stop any existing application on port 8080
    echo [INFO] You can find the process using: netstat -ano | findstr ":8080"
    echo [INFO] And kill it using: taskkill /PID [PID_NUMBER] /F
    set /p continue_port="Press Enter to continue anyway, or Ctrl+C to exit..."
)

REM Start the application
echo.
echo [INFO] Starting the application...
echo [INFO] This will start the Spring Boot application on port 8080

echo [INFO] Starting Spring Boot application...
echo [INFO] Application logs will be written to app.log

REM Start the application
start /b mvn spring-boot:run > app.log 2>&1

REM Wait for application to start
echo [INFO] Waiting for application to start...
echo [INFO] This may take 30-60 seconds...

REM Simple wait loop (Windows doesn't have timeout with curl check easily)
for /L %%i in (1,1,30) do (
    timeout /t 2 /nobreak >nul
    curl -s http://localhost:8080/api/chat/health >nul 2>&1
    if !errorlevel! equ 0 (
        echo [SUCCESS] Application started successfully! ✓
        goto :started
    )
    echo Waiting... (%%i/30)
)

echo [ERROR] Application failed to start within 60 seconds
echo [ERROR] Check app.log for details
pause
exit /b 1

:started
echo.
echo 🎉 Setup Complete!
echo ==================
echo.
echo 🌐 Web Interface: http://localhost:8080/static/index.html
echo 📚 Swagger API:   http://localhost:8080/swagger-ui.html
echo 🔧 H2 Console:    http://localhost:8080/h2-console
echo 📊 API Health:    http://localhost:8080/api/system/health
echo 📖 API Docs:      http://localhost:8080/api-docs
echo.
echo 🧪 Try these sample questions:
echo   • How do I fix Apache server startup issues?
echo   • What causes Tomcat OutOfMemoryError?
echo   • Show me WebSphere cluster problems
echo   • What are common WebLogic issues?
echo.
echo 💡 Database Access (H2 Console):
echo   JDBC URL: jdbc:h2:file:./middleware_tickets_db
echo   Username: sa
echo   Password: password
echo.
echo 📝 To view logs: type "app.log" or use: powershell "Get-Content app.log -Wait"
echo.
echo 🛑 To stop the application: Close this window or press Ctrl+C

REM Open browser automatically
echo [INFO] Opening web interface in your default browser...
start http://localhost:8080/static/index.html

echo.
echo Press any key to stop the application...
pause >nul

REM Cleanup is automatic when window closes
echo [INFO] Stopping application...
