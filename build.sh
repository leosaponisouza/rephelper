#!/bin/bash
echo "Setting execute permissions on mvnw..."
chmod +x ./mvnw

echo "Building with Maven..."
./mvnw clean package -DskipTests

echo "Build completed successfully!" 