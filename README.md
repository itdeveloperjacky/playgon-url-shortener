# playgon-url-shortener

# URL Shortener

This is a simple assignment that will shorten URL implemented in Java using Spring Boot. 
The application allows creating short URLs for long URLs and redirects the 
short URLs to their original long URLs.

## Features

- Create short URLs
- Redirect short URLs to original long URLs
- Track URL access statistics

## Prerequisites

- Java 21
- Docker
- MySQL (AWS RDS) and H2

## Setup

### Database

This application uses different databases for development and production environments:

- **Development**: Uses H2 in-memory database.
- **Production**: Uses Amazon RDS for MySQL as its database. Ensure you have an RDS instance set up with the necessary configurations.

### Configuration Files

The application uses profile-specific configuration files:

- **application-dev.properties**: Used in the development environment, configured to use the H2 in-memory database.
- **application-prod.properties**: Used in the production environment, configured to use Amazon RDS for MySQL.


### Setting Up Environment Variables

To connect to the Amazon RDS, you need to set the following environment variables:

- `DB_URL`: The JDBC URL of your Amazon RDS instance.
- `DB_USERNAME`: The username for your RDS database.
- `DB_PASSWORD`: The password for your RDS database.

You can set these environment variables in your shell or include them in a `.env` file if using Docker Compose.

### Running the Application with Docker

For local/dev

mvn spring-boot:run -Dspring-boot.run.profiles=dev

or

For prod
mvn spring-boot:run -Dspring-boot.run.profiles=prod

1. Build the Docker image:

    docker build -t url-shortener:latest .


2. Run the Docker container:

For H2

   docker run -e "SPRING_PROFILES_ACTIVE=dev" -p 8082:8081 url-shortener:latest


For AWS RDS

   docker run -e "SPRING_PROFILES_ACTIVE=prod" -e DB_USERNAME=yourUsername -e DB_PASSWORD=yourPassword -p 8082:8081 url-shortener:latest


3. Access the application:

    - Create short URL: `POST http://localhost:8082/shorten`
    - Redirect URL: `GET http://localhost:8082/{shortUrl}`
    - Statistics: `GET http://localhost:8082/api/stats/top`, `GET http://localhost:8082/api/stats/count/{shortUrl}`

## Endpoints

- **POST /shorten**: Create a short URL.
    - Request Body: `{"longUrl": "https://example.com"}`
    - Response: `{"shortUrl": "http://localhost:8082/abc123"}`

- **GET /{shortUrl}**: Redirect to the original long URL.
    - Example: `http://localhost:8082/abc123`

- **GET /api/stats/top**: Get top 10 most accessed short URLs.

- **GET /api/stats/count/{shortUrl}**: Get access count for a specific short URL.

## Configuration

The application configuration can be adjusted in the `application.properties` file.
