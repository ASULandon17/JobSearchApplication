Write-Host "Cleaning project..." -ForegroundColor Green
mvn clean

Write-Host "Resolving dependencies..." -ForegroundColor Green
mvn dependency:resolve

Write-Host "Compiling..." -ForegroundColor Green
mvn compile

Write-Host "Running application..." -ForegroundColor Green
mvn exec:java