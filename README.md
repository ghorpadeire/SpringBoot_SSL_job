# SSL/TLS Assessment Application

A simple web application for performing remote SSL/TLS assessment of HTTPS servers.

## Features

- **Hostname Assessment**: Enter a hostname to assess its SSL/TLS configuration
- **Recent Assessments**: View the 10 most recent assessments with key information
- **Certificate Validation**: Check if certificates are valid for the given hostname
- **Protocol & Cipher Info**: See negotiated SSL/TLS protocol and cipher suite
- **Cache Management**: Clear individual assessments to re-test servers

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Frontend**: HTML, JavaScript (Vanilla JS)
- **Storage**: In-memory cache (no database required)

## Requirements

- Java 17 or higher
- Maven 3.6+

## Running the Application

### Local Development

1. Clone the repository:
```bash
git clone 
cd SpringBoot_SSL_job
```

2. Build the application:
```bash
mvn clean package
```

3. Run the application:
```bash
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/ssl-assessment-app-1.0.0.jar
```

4. Access the application:
```
http://localhost:8080
```

## API Endpoints

- `POST /api/assess` - Perform SSL assessment for a hostname
- `GET /api/assessments` - Get list of recent assessments
- `GET /api/assessment/{hostname}` - Get specific assessment result
- `DELETE /api/assessment/{hostname}` - Delete cached assessment

## Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8080                # Application port
app.max.assessments=10          # Max assessments to keep in cache
app.ssl.timeout=10000          # SSL connection timeout (ms)
```

## Usage

1. Open the home page
2. Enter a valid hostname (e.g., `google.com`, `github.com`)
3. Click "Assess" to start the assessment
4. View results on the results page
5. Use "Clear Cache" to delete an assessment and re-test

## Deployment

### Deploy to Cloud Platform

The application can be easily deployed to:
- Heroku
- Google Cloud Platform
- AWS Elastic Beanstalk
- Azure App Service

Simply package as JAR and deploy following platform-specific instructions.

### Docker (Optional)

Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/ssl-assessment-app-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t ssl-assessment-app .
docker run -p 8080:8080 ssl-assessment-app
```

## Limitations

- IP addresses and ports are not accepted, only valid hostnames
- Assessments are stored in memory (cleared on restart)
- Limited to 10 most recent assessments
- Simple certificate validation (production use may require more thorough checks)

## Security Notes

- The application connects to external servers on port 443
- Certificate validation uses Java's built-in SSL libraries
- Input validation prevents invalid hostnames and potential injection attacks

## License

MIT
