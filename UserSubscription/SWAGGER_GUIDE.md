# API Documentation Guide - Swagger/OpenAPI

This guide explains how to access, use, and customize the API documentation for the User Subscription Service.

## Quick Access

Once the application is running on `http://localhost:8080`, access the documentation at:

- **Swagger UI (Interactive)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- **ReDoc Alternative**: [http://localhost:8080/redoc.html](http://localhost:8080/redoc.html)

## What is Swagger/OpenAPI?

Swagger (now part of OpenAPI Initiative) is an open specification for describing REST APIs. It provides:

- **Interactive API Explorer** - Test endpoints directly from your browser without Postman
- **Automatic Documentation** - Generated directly from Java annotations
- **Request/Response Examples** - See the exact format expected for each endpoint
- **Schema Validation** - Understand all possible request/response structures
- **Authentication Support** - Built-in handling for API security
- **Code Generation** - Generate client libraries in 40+ languages

## Accessing the API Documentation

### 1. Start the Application

```bash
# From UserSubscription directory
mvn spring-boot:run

# Or run the JAR
java -jar target/user-subscription-1.0.0.jar
```

### 2. Open Swagger UI

Navigate to `http://localhost:8080/swagger-ui.html` in your browser.

You'll see:
- All REST endpoints organized by resource type
- HTTP method color-coded (POST=blue, GET=green, PUT=orange, DELETE=red)
- Request/response examples
- Parameter descriptions
- Error response codes

## Using the API Through Swagger UI

### Testing the "Create User" Endpoint

1. **Find the endpoint**: Search for or scroll to `POST /api/v1/users`
2. **Expand the endpoint**: Click on it to see full details
3. **Click "Try it out"**: This enables the test interface
4. **Enter request body**: Fill in the input fields:
   ```json
   {
     "name": "John Doe",
     "age": 30,
     "city": "San Francisco",
     "company": "Tech Corp",
     "interests": ["Java", "Spring", "Cloud"]
   }
   ```
5. **Execute**: Click "Execute" to send the request
6. **View response**: See response code (201 Created), headers, and response body with:
   - Generated UUID (e.g., "id": "550e8400-e29b-41d4-a716-446655440000")
   - Current timestamp (e.g., "createdAt": 1709865000000)

### Testing the "Get All Users" Endpoint

1. Find `GET /api/v1/users`
2. Click "Try it out"
3. Click "Execute"
4. View list of all users in the system

### Testing the "Get User by ID" Endpoint

1. Find `GET /api/v1/users/{id}`
2. Click "Try it out"
3. Enter a user ID from a previous operation
4. Click "Execute"
5. View the specific user details

### Testing the "Update User" Endpoint

1. Find `PUT /api/v1/users/{id}`
2. Click "Try it out"
3. Enter user ID and updated data:
   ```json
   {
     "name": "Jane Doe",
     "age": 31,
     "city": "New York",
     "company": "Finance Corp",
     "interests": ["Data Science", "Analytics"]
   }
   ```
4. Click "Execute"
5. Verify the response shows updated data with new `updatedAt` timestamp

### Testing the "Delete User" Endpoint

1. Find `DELETE /api/v1/users/{id}`
2. Click "Try it out"
3. Enter a user ID
4. Click "Execute"
5. View 204 No Content response (successful deletion)

### Checking Health and Statistics

1. Find `GET /api/v1/users/health` - Returns system health status
2. Find `GET /api/v1/users/stats/count` - Returns total number of users in system

## API Endpoints Overview

All endpoints are accessible through the Swagger UI with full documentation:

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/api/v1/users` | Create new user | 201 Created |
| GET | `/api/v1/users` | List all users | 200 OK |
| GET | `/api/v1/users/{id}` | Get user by ID | 200 OK or 404 |
| PUT | `/api/v1/users/{id}` | Update user | 200 OK or 404 |
| DELETE | `/api/v1/users/{id}` | Delete user | 204 No Content or 404 |
| GET | `/api/v1/users/stats/count` | Get user count | 200 OK |
| GET | `/api/v1/users/health` | Health check | 200 OK |

## Understanding the User Model

In Swagger UI, scroll to the bottom to see "Schemas" section which shows the User model structure:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",  // UUID (auto-generated)
  "name": "John Doe",                              // Required
  "age": 30,                                       // Optional, 0-150
  "city": "San Francisco",                         // Optional
  "company": "Tech Corp",                          // Optional
  "interests": [                                   // Optional array
    "Java",
    "Spring",
    "Cloud"
  ],
  "createdAt": 1709865000000,                     // Timestamp (milliseconds since epoch)
  "updatedAt": 1709865000000                      // Timestamp (milliseconds since epoch)
}
```

## Response Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | GET, PUT endpoints successful |
| 201 | Created | POST endpoint successful |
| 204 | No Content | DELETE successful (no response body) |
| 400 | Bad Request | Invalid request data |
| 404 | Not Found | User ID doesn't exist |
| 500 | Server Error | Unexpected server error |

## Using Swagger with Different Profiles

### Local Development (MySQL on localhost)

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Then access: http://localhost:8080/swagger-ui.html
```

### OCI Autonomous Database

```bash
export SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<hostname>:<port>/<service_name>
export SPRING_DATASOURCE_USERNAME=admin
export SPRING_DATASOURCE_PASSWORD=<password>
export SPRING_PROFILES_ACTIVE=oci

mvn spring-boot:run

# Swagger UI works the same way: http://localhost:8080/swagger-ui.html
```

### In-Memory Testing

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--app.persistence.type=in-memory"

# Data is not persisted between restarts
```

## Customizing Swagger Documentation

To modify the API title, version, description, or contact info:

1. Open `src/main/java/com/agentic/subscription/config/OciConfiguration.java`
2. Find the `customOpenAPI()` method
3. Update the `Info` object:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("User Subscription API")              // Change API title
            .version("1.0.0")                            // Update version
            .description("REST API for...")               // Update description
            .contact(new Contact()
                .name("Your Team Name")
                .email("support@example.com"))
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
}
```

4. Restart the application - Swagger UI updates automatically

## Generating Downloadable OpenAPI Specs

The OpenAPI specification can be downloaded in multiple formats:

### JSON Format
Download from: `http://localhost:8080/api-docs`

```bash
# Example: Save to file
curl http://localhost:8080/api-docs > api-spec.json

# Use with other tools (like generating client libraries)
```

### YAML Format
Some tools support YAML format of OpenAPI specs.

## Using API Specs with Tools

### 1. Generate Client Libraries

Using the OpenAPI spec, generate client libraries for your frontend or mobile app:

```bash
# Install openapi-generator
npm install @openapitools/openapi-generator-cli

# Generate TypeScript client
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/api-docs \
  -g typescript-fetch \
  -o ./generated-client
```

### 2. API Testing with Postman

Import the OpenAPI spec into Postman:
1. Open Postman
2. Click "File" → "Import"
3. Enter: `http://localhost:8080/api-docs`
4. All endpoints are automatically imported with proper structure

### 3. API Mocking

Use Swagger UI to understand the API structure for mocking in tests:
- See request/response formats
- Understand validation rules
- Learn error scenarios

## JavaDoc Documentation

In addition to Swagger UI, comprehensive JavaDoc is available:

### View JavaDoc

1. **Generate**: `mvn javadoc:javadoc`
2. **Open**: `open target/site/apidocs/index.html`
3. **Browse**: Navigate through packages and classes
4. **Search**: Use the search bar to find specific methods

### JavaDoc Includes

- **Class documentation**: Purpose, usage, design patterns
- **Method documentation**: What the method does, parameters, return values
- **Parameter documentation**: Constraints, valid ranges, examples
- **Example usage**: Code examples for important methods
- **Integration details**: How components interact

## API Documentation Best Practices

### For API Consumers
1. Always check Swagger UI first for endpoint documentation
2. Use "Try it out" feature to understand request format
3. Read response schemas to understand data structure
4. Check response codes to understand error handling
5. Review examples for complex operations

### For API Developers
1. Keep JavaDoc comments updated as API changes
2. Use meaningful endpoint descriptions
3. Document all response codes and errors
4. Provide request/response examples
5. Update version number in Swagger when making breaking changes

## Troubleshooting

### Swagger UI Not Loading
- Ensure application is running: `curl http://localhost:8080/swagger-ui.html`
- Check logs for SpringDoc initialization errors
- Verify `springdoc-openapi` dependency in pom.xml

### Endpoints Not Showing
- Restart the application
- Check that controller methods have `@Operation` annotations
- Verify `@RestController` and `@RequestMapping` are present

### CORS Issues (when accessing from different domain)
- Add CORS configuration to Spring Security if needed
- Swagger UI should work on same domain as API

## Further Reading

- [OpenAPI Official Spec](https://spec.openapis.org/oas/v3.0.3)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [Swagger Editor](https://editor.swagger.io/)

## Support

For issues or questions about the API:
1. Check Swagger UI documentation for endpoint details
2. Review JavaDoc for code-level documentation
3. Run E2E tests to see actual usage examples
4. Check logs for detailed error messages
