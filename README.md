# Smart Campus API — 5COSC022W

## Overview
A JAX-RS RESTful API for managing campus Rooms and Sensors built 
with Jersey 2.41 and Grizzly embedded server. Uses in-memory 
HashMaps for data storage.

## Technology Stack
- Java 8
- JAX-RS (Jersey 2.41)
- Grizzly HTTP Server
- Maven

## How to Build and Run
1. Clone the repository:
   git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
2. Open NetBeans → File → Open Project → select CSA-CW folder
3. Right-click project → Build with Dependencies
4. Right-click Main.java → Run File
5. Server starts at: http://localhost:8080/api/v1/

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/discovery | Discovery endpoint |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a room |
| GET | /api/v1/rooms/{roomId} | Get a room |
| DELETE | /api/v1/rooms/{roomId} | Delete a room |
| GET | /api/v1/sensors | List all sensors |
| POST | /api/v1/sensors | Register a sensor |
| GET | /api/v1/sensors/{sensorId} | Get a sensor |
| GET | /api/v1/sensors/{sensorId}/readings | Get readings |
| POST | /api/v1/sensors/{sensorId}/readings | Add reading |

## Sample curl Commands

### 1. Discovery
curl -X GET http://localhost:8080/api/v1/discovery

### 2. Get all rooms
curl -X GET http://localhost:8080/api/v1/rooms

### 3. Create a room
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-201","name":"Engineering Lab","capacity":40}'

### 4. Filter sensors by type
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"

### 5. Add a reading
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.5}'

### 6. Try invalid roomId (422)
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'

### 7. Delete room with sensors (409)
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301


Report Answers

Part 1.1 — JAX-RS Lifecycle

By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request (request-scoped). This means instance variables are not shared between requests and would be lost after each request completes. To solve this for in-memory storage, all shared data is stored in a dedicated DataStore class using static HashMaps. Static fields belong to the class itself rather than any instance, so they persist for the entire lifetime of the server. In a concurrent environment, multiple simultaneous requests could cause race conditions when modifying these maps. To prevent data corruption in production, ConcurrentHashMap should be used instead of HashMap, as it provides thread-safe operations without requiring explicit synchronisation blocks.

Part 1.2 — HATEOAS

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include hyperlinks to related resources and available next actions, rather than just raw data. This is considered a hallmark of advanced RESTful design because it makes the API self-documenting and self-discoverable. Client developers can navigate the entire API by following embedded links, without needing to memorise or hard-code any URLs. Compared to static documentation, which quickly becomes outdated as the API evolves, hypermedia links are always accurate because they come directly from the live running API. This significantly reduces the coupling between client and server — if a URL changes on the server side, clients that follow links dynamically adapt automatically, whereas clients relying on static docs would break immediately.

Part 2.1 — ID-only vs Full Object Returns

Returning only IDs in a list response minimises network bandwidth and payload size, which is beneficial when the list contains thousands of rooms. However, the client must then make additional HTTP requests to fetch the details of each room it needs, significantly increasing the total number of round trips and adding latency. Returning full room objects in a single list response provides all data in one request, reducing round trips and simplifying client-side processing considerably. The trade-off is a larger initial payload. Best practice depends on context: for very large collections, pagination with summary objects should be used; for small collections like this Smart Campus system, returning full objects is perfectly acceptable and more convenient for clients.

Part 2.2 — DELETE Idempotency

Yes, the DELETE operation in this implementation is idempotent. Idempotency means that making the same request multiple times produces the same server state as making it once. In this API, the first DELETE call on a room that exists and has no sensors will remove it from the HashMap and return 200 OK. If the identical DELETE request is sent again, the room no longer exists, so the server returns 404 Not Found. The server state is identical after both calls — the room is absent. While the response code differs between the first and subsequent calls (200 vs 404), the actual resource state does not change after the initial deletion, which fully satisfies the definition of idempotency as defined in the HTTP specification (RFC 7231).

Part 3.1 — @Consumes Mismatch

The @Consumes(MediaType.APPLICATION_JSON) annotation instructs JAX-RS that the POST endpoint only accepts requests with a Content-Type: application/json header. If a client sends data with a different content type such as text/plain or application/xml, JAX-RS automatically rejects the request before it even reaches the resource method. The runtime returns an HTTP 415 Unsupported Media Type response. This behaviour is enforced entirely by the JAX-RS framework through the annotation — the developer does not need to write any custom validation code for content-type checking. This makes input validation at the media type level automatic, consistent, and reliable across all endpoints in the application.

Part 3.2 — QueryParam vs PathParam for Filtering

Query parameters such as GET /sensors?type=CO2 are superior to path parameters for filtering collections for several key reasons. First, they are optional by nature — a request to /sensors without any query parameter still works and returns all sensors, whereas a path segment like /sensors/type/CO2 would require a completely separate route definition. Second, multiple query parameters can be combined easily, for example ?type=CO2&status=ACTIVE, without changing the resource path structure. Third, path parameters are semantically designed to identify a specific unique resource by its identity, not to filter a collection. Using /sensors/type/CO2 incorrectly implies that type/CO2 is a unique resource identifier. Query parameters correctly communicate that the client is searching or filtering within the /sensors collection, which aligns with REST conventions and makes the API more intuitive.

Part 4.1 — Sub-Resource Locator Pattern Benefits

The Sub-Resource Locator pattern allows a parent resource class to delegate handling of a nested path to a completely separate dedicated class. Instead of putting all /readings logic inside SensorResource, a dedicated SensorReadingResource class is instantiated and returned by the locator method. This provides several architectural benefits. First, it enforces the Single Responsibility Principle — each class manages exactly one resource. Second, it makes large codebases significantly easier to navigate and maintain, as related functionality is grouped together in its own class. Third, in APIs with many nested resources, keeping all logic in one controller would result in an enormous, unmanageable class. Delegation keeps each class small and focused. Finally, SensorReadingResource can be independently tested without loading the entire sensor context, improving testability.

Part 5.1 — 422 vs 404

HTTP 404 Not Found means the requested URL endpoint itself does not exist on the server. HTTP 422 Unprocessable Entity means the endpoint was found and the JSON payload was syntactically valid, but the server could not process the request because the business logic failed. When a client sends a valid POST request to /sensors with a roomId that does not exist, the endpoint /sensors clearly exists — so 404 is semantically incorrect. The problem is that the data inside the payload references a non-existent linked resource. HTTP 422 is more accurate because it communicates that the request was received, understood, and parsed correctly, but the referenced entity inside the payload could not be resolved. This gives the client a precise signal that they need to correct the data in their request body, not the URL they are calling.

Part 5.2 — Stack Trace Security Risks

Exposing Java stack traces to external API consumers presents serious cybersecurity risks. First, stack traces reveal internal file paths and package structure (e.g., com.smartcampus.resource.RoomResource.java:45), disclosing the application's internal architecture to potential attackers. Second, they expose the names and exact versions of third-party libraries such as Jersey and Jackson, which attackers can cross-reference against known CVE vulnerability databases to identify specific exploitable weaknesses. Third, stack traces can reveal the internal logic and processing flow of the application, helping attackers identify injection points or logic flaws. Fourth, line numbers in traces help attackers pinpoint exactly where specific processing occurs in the source code. The global ExceptionMapper<Throwable> prevents all of this by intercepting every unhandled exception and returning only a safe, generic error message to the client, while logging the full technical details internally where only authorised personnel can access them.

Part 5 Filter — Why Filters over Manual Logging

JAX-RS filters are superior for cross-cutting concerns like logging because they enforce consistency and completely eliminate code duplication. If Logger.info() calls were manually inserted into every resource method, the same boilerplate would need to be added to dozens of methods across multiple classes. If a developer forgets to add logging to even one method, that endpoint goes completely unmonitored. Filters registered with @Provider automatically apply to every single request and response without any modifications to individual resource classes. This also means logging logic is centralised — if the log format needs to change, only one class needs to be updated rather than every method in the application. This perfectly demonstrates the principle of Separation of Concerns: resource methods focus exclusively on business logic, while filters handle infrastructure concerns such as logging, authentication, and compression.



