# Step 1: Use the official OpenJDK 21 image as the base image
FROM openjdk:21-jdk-slim

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy the built JAR file into the container
COPY target/*.jar app.jar

# Step 4: Expose the application port (8080)
EXPOSE 8080

# Step 5: Define the entry point for the container to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
