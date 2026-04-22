# Smart Campus Sensor & Room Management API
Module **5COSC022W** – Client-Server Architectures <br>
Coursework - REST API Design and Implementation using JAX-RS

---

## Student Details

| Field | Details |
|---|---|
| **Name** | R.M.Nipun Nirman |
| **UoW Number** | w2151918 |
| **IIT Number** | 20241764 |
| **Module** | 5COSC022W – Client-Server Architectures |

---

## 1. API Overview
This project implements a RESTful API for managing smart campus infrastructure, specifically rooms, sensors, and sensor readings.

The API allows administrators and systems to:

* Register and manage rooms across the campus
* Register sensors and associate them with rooms
* Record historical sensor readings
* Retrieve filtered sensor information
* Maintain system integrity through validation and error handling

The system follows the REST architectural style, where resources are accessed through standard HTTP methods such as:

* GET – retrieve resources
* POST – create resources
* DELETE – remove resources

The API is implemented using JAX-RS (Jersey 2.41), Java 11, Maven, and deployed on Apache Tomcat 9. All data is stored in-memory using `HashMap` and `ArrayList` — no database is used.

<br>

### API Routes

Base API URL - `http://localhost:8080/smart-campus-api/api/v1`

<br>

| Resource | Base Path |
|---|---|
| Discovery Endpoint | `GET /api/v1` |
| Get all rooms | `GET /api/v1/rooms` |
| Create room | `POST /api/v1/rooms` |
| Get Room by ID | `GET /api/v1/rooms/{roomId}` |
| Delete room | `DELETE /api/v1/rooms/{roomId}` |
| Get All Sensors | `GET /api/v1/sensors` |
| Filter Sensors by Type | `GET /api/v1/sensors?type=CO2` |
| Create Sensor | `POST /api/v1/sensors` |
| Get Sensor Readings | `GET /api/v1/sensors/{sensorId}/readings` |
| Add Sensor Reading | `POST /api/v1/sensors/{sensorId}/readings` |

<br>

### Exception Mapping
| Exception | HTTP Code | Scenario |
|---|---|---|
| NotFoundExceptionMapper | 404 | Resource not found |
| RoomNotEmptyException | 409 | Cannot delete room with sensors |
| LinkedResourceNotFoundException | 422 | Invalid room reference in sensor payload |
| SensorUnavailableException | 403 | Sensor is under MAINTENANCE |
| GlobalExceptionMapper | 500 | Unexpected server error |

<br>

### Project Structure
```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/app/
    ├── SmartCampusApplication.java
    │
    ├── api/
    │   ├── DiscoveryResource.java
    │   ├── SensorReadingResource.java
    │   ├── SensorResource.java
    │   └── SensorRoomResource.java
    │
    ├── error/
    │   ├── GlobalExceptionMapper.java
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java
    │   ├── NotFoundExceptionMapper.java
    │   ├── RoomNotEmptyException.java
    │   ├── RoomNotEmptyExceptionMapper.java
    │   ├── SensorUnavailableException.java
    │   └── SensorUnavailableExceptionMapper.java
    │
    ├── filter/
    │   └── RequestResponseLoggingFilter.java
    │
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    │
    └── store/
        └── CampusData.java
```
<hr>

<br>

## 2. How to Build & Run the Project

1. Clone the repository:
   ```
   git clone https://github.com/YOUR_GITHUB_USERNAME/smart-campus-api.git
   ```

2. Open in Apache NetBeans or IntelliJ IDEA:
   * File → Open Project
   * Navigate to the cloned folder → Click Open

3. Ensure Apache Tomcat 9 is configured and running

4. Build the project using Maven:
   ```
   mvn clean package
   ```

5. Copy the WAR file to Tomcat:
   ```
   cp target/smart-campus-api.war $CATALINA_HOME/webapps/
   ```

6. Start Tomcat:
   ```
   /Applications/apache-tomcat-9.0.117/bin/startup.sh
   ```

The API will start at:

`http://localhost:8080/smart-campus-api/api/v1`

<hr>

<br>

## 3. Sample cURL Commands

1. Discovery Endpoint
```
curl http://localhost:8080/smart-campus-api/api/v1
```

2. Get All Rooms
```
curl http://localhost:8080/smart-campus-api/api/v1/rooms
```

3. Create a Room
```
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library quiet study\",\"capacity\":40}"
```

4. Create a Sensor
```
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"LIB-301\"}"
```

5. Filter Sensors by Type
```
curl http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2
```

6. Post a Sensor Reading
```
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":450.5,\"timestamp\":0}"
```

7. Get All Readings for a Sensor
```
curl http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings
```

8. Trigger 409 – Delete a Room That Still Has Sensors
```
curl -s -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

9. Trigger 422 – Create Sensor with Non-Existent roomId
```
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"FAKE-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"DOES-NOT-EXIST\"}"
```

10. Trigger 403 – Post Reading to a MAINTENANCE Sensor
```
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/HUM-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":65.0,\"timestamp\":0}"
```

<hr>

<br>

## 4. Report – Answers to Coursework Questions

### Part 1: Service Architecture & Setup

**Q1.1 Project & Application Configuration**

In JAX-RS, resource classes are instantiated per request by default. This means that every incoming HTTP request creates a new instance of the resource class that handles that request. The JAX-RS runtime invokes the appropriate method on that instance and then discards it after the request is completed.

This lifecycle behaviour improves thread safety because each request is handled by a separate object instance, preventing shared mutable state inside the resource class. Unlike a singleton resource, which would share the same instance across multiple threads and require explicit synchronisation, the per-request model avoids this issue entirely. However, it also means that in-memory data structures used to store application data (such as `HashMap` or `ArrayList`) cannot be defined as instance variables inside the resource class, because they would be recreated with every request and all data would be lost.

To prevent this, the shared data store is implemented as a separate class (`CampusData`) and injected into every resource using HK2 dependency injection. A single `CampusData` instance is created at application startup and bound through `AbstractBinder`, ensuring all resource instances share the same store across requests.

Since web servers handle many requests concurrently, all methods in `CampusData` are declared `synchronized` to prevent race conditions when multiple threads read and write the shared `HashMap` collections simultaneously. This design ensures that application data persists across requests while keeping resource classes stateless, which aligns with the REST constraint that each request should be self-contained without relying on server-side session state.

<br>

**Q1.2 The "Discovery" Endpoint**

Hypermedia as the Engine of Application State (HATEOAS) is a core REST principle where API responses include links that guide the client to related resources and available actions. Instead of requiring clients to rely on external documentation or hardcoded URLs, the API itself provides navigational information within each response.

In this project, the discovery endpoint at `GET /api/v1` returns metadata including the API version, admin contact, and a `_links` map containing the full URLs to the rooms and sensors collections. This allows a client to start from this single endpoint and navigate the entire API dynamically by following the provided links.

This approach provides several advantages. First, it improves API discoverability — a client can begin using the API by calling only the root URL and then follow links to find everything else. Second, it reduces coupling between client and server — if URLs change in future versions, clients that follow links dynamically will adapt automatically without code changes. Third, it makes the API self-describing and easier to evolve over time, compared to static documentation that can become outdated whenever endpoint paths are modified.

<br>

### Part 2: Room Management

**Q2.1 Room Resource Implementation**

Returning only room IDs significantly reduces the size of each response payload, which minimises network bandwidth usage and improves performance, particularly when a large number of rooms exist. However, clients must then make additional requests to retrieve detailed information for each room, which increases the total number of API calls and can reduce overall efficiency.

Returning full room objects provides more complete information in a single response. Clients receive all relevant data such as the room name, capacity, and associated sensor IDs without needing additional network round trips.

The design trade-off involves balancing payload size against client convenience and the number of required calls. Returning only IDs reduces payload overhead but increases request overhead. Returning full objects increases payload size but reduces the number of client requests.

In this API, returning full room objects is the appropriate choice because the room data model is relatively small, meaning the additional payload overhead is minimal while significantly improving usability for client applications. For very large datasets, returning only IDs or lightweight summary objects would be the more efficient option.

<br>

**Q2.2 Room Deletion & Safety Logic**

Yes, in this implementation the DELETE operation is idempotent.

An HTTP method is considered idempotent when performing the same request multiple times produces the same final server state. In the Smart Campus API, deleting a room removes it from the in-memory `HashMap`. If a client sends the same DELETE request again for the same room ID, the room no longer exists and a 404 Not Found response is returned.

Therefore:
* If the room exists and has no sensors assigned, it is removed and 204 No Content is returned.
* If the same DELETE request is sent again, the room is already gone and 404 Not Found is returned.
* The final state of the server is the same in both cases — the room does not exist.

Even though the HTTP response codes differ between the first and subsequent calls, the server state outcome remains identical. Because repeated DELETE operations produce the same end state without creating additional side effects, the operation fully satisfies the definition of idempotency.

<br>

### Part 3: Sensor Operations & Linking

**Q3.1 Sensor Resource & Integrity**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation specifies that the resource method only accepts requests with a JSON content type. When a client sends a request with a different `Content-Type` header such as `text/plain` or `application/xml`, the JAX-RS runtime checks the incoming header against the media types declared in the annotation.

If the `Content-Type` does not match, the server cannot deserialise the request body into the expected Java object. In this situation, JAX-RS automatically rejects the request and returns an HTTP **415 Unsupported Media Type** response before the resource method is even invoked. No custom code is required for this behaviour — it is handled entirely by the framework. This ensures that the server only processes requests in formats it can understand and prevents malformed or incorrectly formatted data from reaching the application logic.

<br>

**Q3.2 Filtered Retrieval & Search**

Filtering operations represent optional criteria applied to a collection resource, and query parameters are specifically designed for this purpose. Using `GET /api/v1/sensors?type=CO2` clearly communicates that the client is requesting a filtered subset of the sensors collection. In contrast, embedding the filter value in the URL path such as `/api/v1/sensors/type/CO2` implies that `type` is a sub-resource or a distinct addressable entity, rather than a search condition.

Query parameters offer several advantages. First, they allow multiple filter conditions to be combined naturally, for example `/sensors?type=CO2&status=ACTIVE`. The equivalent path-based approach would require a new route definition for every possible combination of filter fields, which becomes unmanageable as the API grows. Second, they keep the main collection endpoint clean and consistent — a single `GET /sensors` endpoint handles all filter combinations using `@QueryParam`. Third, they follow widely adopted REST conventions used by major APIs, making the interface more intuitive for client developers.

For these reasons, query parameters are the standard and preferred approach for filtering and searching collections in RESTful APIs.

<br>

### Part 4: Deep Nesting with Sub-Resources

**Q4.1 The Sub-Resource Locator Pattern**

The Sub-Resource Locator pattern allows nested resources to be handled by dedicated classes rather than placing all logic inside a single large resource class. In this API, sensor readings are accessed through the path `/api/v1/sensors/{sensorId}/readings`. Instead of implementing all reading-related logic inside `SensorResource`, a dedicated `SensorReadingResource` class handles all requests at the `/readings` level.

The sub-resource locator method in `SensorResource` is annotated only with `@Path("{sensorId}/readings")` and no HTTP verb annotation. When a request matches this path, the JAX-RS runtime calls this method to obtain a `SensorReadingResource` instance and then delegates the actual request to that object. This allows routing to be resolved dynamically at runtime rather than statically at compile time.

This design provides several important advantages. First, it enforces separation of concerns — `SensorResource` is responsible only for sensor management, while `SensorReadingResource` focuses entirely on reading history. Second, it improves maintainability by keeping each class small and focused, making it easier to extend or modify without introducing unintended side effects in unrelated parts of the code. Third, it improves readability and scalability — as the API grows with additional nested resources, the sub-resource pattern keeps the codebase modular and manageable. Without this pattern, a single resource class handling all nested paths would become excessively large and difficult to maintain.

<br>

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q5.1 Dependency Validation (422 Unprocessable Entity)**

HTTP 404 indicates that the requested endpoint URL does not exist on the server. When a client attempts to register a sensor with a `roomId` that does not exist, the request URL `/api/v1/sensors` is completely valid and the endpoint does exist. The problem is not a missing URL — it is that the data inside the request body contains a semantic error, specifically a reference to a resource that cannot be resolved.

HTTP 422 Unprocessable Entity is more semantically accurate in this scenario because it signals that the server understood the request format and the content type was correct, but it was unable to process the instructions due to a logical error within the payload. Returning 404 would mislead the client into thinking the endpoint itself does not exist, whereas 422 precisely communicates that the request structure was valid but the referenced resource ID was not found. This distinction helps client developers diagnose the issue correctly and fix the data rather than the URL.

<br>

**Q5.2 The Global Safety Net (500)**

Exposing raw Java stack traces in API responses is one of the most common and dangerous information vulnerabilities in web services. A single stack trace can provide an attacker with a detailed report about the internal structure of the system. The specific information categories an attacker can harvest include:

* **Technology stack and versions:** Package and class names immediately reveal the frameworks in use, the Java version, and any third-party libraries with their exact version numbers. The attacker can then cross-reference these versions against published CVEs to identify known, unpatched vulnerabilities.

* **Internal file paths:** Stack traces commonly include absolute file system paths that reveal the server's directory structure, operating system, and deployment layout.

* **Application logic and control flow:** The call stack exposes the exact sequence of method invocations that led to the error, including class names, method names, and line numbers. This reveals the internal architecture of the application and shows precisely where and how input is processed, making it easier to identify injection points.

* **Database and configuration details:** If an unhandled exception originates from a database driver, connection pool, or configuration loader, the trace may inadvertently expose connection strings, hostnames, port numbers, or file paths to configuration files containing credentials.

The `GlobalExceptionMapper` in this project implements `ExceptionMapper<Throwable>`, catching any unexpected runtime error and returning a generic HTTP 500 response with a safe, non-descriptive message. All detailed error information is logged server-side only, keeping the application completely leak-proof to external consumers.

<br>

**Q5.3 API Request & Response Logging Filters**

JAX-RS filters allow cross-cutting concerns such as logging to be handled at the framework level, intercepting every incoming request and outgoing response automatically. Using a `ContainerRequestFilter` and `ContainerResponseFilter` in a single `RequestResponseLoggingFilter` class offers several advantages over manually inserting `Logger.info()` calls inside every resource method.

First, filters centralise all logging logic in one place. This eliminates code duplication and guarantees consistent log formatting across the entire API regardless of which resource handles the request. Second, filters automatically capture every request and response without requiring any changes to individual resource methods. This provides complete API observability and ensures no endpoint is accidentally left unlogged. Third, cross-cutting concerns such as logging, authentication checking, and monitoring are kept entirely separate from business logic. Resource classes remain clean and focused on their core responsibility, while the filter handles observability independently. This design improves maintainability, makes the application easier to extend in the future, and ensures the codebase follows the single responsibility principle.
