echo Building Spring Boot app with Maven...
call mvn clean package -DskipTests
echo Maven exited with ERRORLEVEL=%ERRORLEVEL%
IF %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting.
    exit /b %ERRORLEVEL%
)

echo Deleting old Docker images...
for /f "tokens=*" %%i in ('docker images -q paymentservice') do (
    echo Removing image %%i
    docker rmi -f %%i
)

echo Starting Docker Compose...
call docker-compose up --build
call docker-compose down --rmi all