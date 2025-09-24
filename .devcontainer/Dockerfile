# Use the official image as a parent image
FROM mcr.microsoft.com/vscode/devcontainers/java:17

# Install Maven
RUN apt-get update && apt-get install -y maven mysql-client

# Set working directory
WORKDIR /workspace

# Copy project files
COPY . .

# Build the project
RUN mvn clean compile

# Expose port
EXPOSE 8082

# Set environment variables for low memory usage
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseSerialGC"

# Default command
CMD ["mvn", "spring-boot:run"]
