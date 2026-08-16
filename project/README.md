# HIVE Backend

A production-quality 1-to-1 realtime chat backend built with Spring Boot, JWT authentication, MongoDB, and STOMP WebSocket.

## Tech Stack

- **Java 21**
- **Spring Boot 3.3**
- **Spring Security** (JWT stateless authentication)
- **Spring Data MongoDB**
- **WebSocket / STOMP** (realtime messaging)
- **Bean Validation**
- **Maven**

## Prerequisites

- Java 21 JDK
- Maven 3.9+
- MongoDB 7.0+ (running locally or a connection URI)

## MongoDB Setup

### Local MongoDB

```bash
# Install MongoDB Community Edition, then start it:
mongod --dbpath /path/to/data
```

The default connection URI is `mongodb://localhost:27017/hive`.

### MongoDB Atlas

Create a free cluster at [mongodb.com/atlas](https://www.mongodb.com/atlas), then set the `MONGODB_URI` environment variable to your Atlas connection string.

## Environment Variables

Copy `.env.example` to `.env` and fill in the values:

| Variable        | Default                  | Description                          |
|-----------------|--------------------------|--------------------------------------|
| `MONGODB_URI`   | mongodb://localhost:27017/hive | MongoDB connection string     |
| `JWT_SECRET`    | (development default)     | HMAC-SHA256 signing key (min 256 bits) |
| `JWT_EXPIRATION`| 86400000 (24 hours)       | JWT token lifetime in milliseconds   |
| `FRONTEND_URL`  | http://localhost:3000     | Allowed CORS origin for the frontend |
| `SERVER_PORT`   | 8080                     | Backend server port                  |

## How to Run

```bash
# Set environment variables (or use .env)
export MONGODB_URI=mongodb://localhost:27017/hive
export JWT_SECRET=your-secret-key-at-least-256-bits-long
export JWT_EXPIRATION=86400000
export FRONTEND_URL=http://localhost:3000

# Build and run
mvn clean spring-boot:run
```

The server starts on port 8080.

## API Endpoints

### Authentication

| Method | Endpoint              | Auth | Description           |
|--------|-----------------------|------|-----------------------|
| POST   | /api/auth/register    | No   | Register a new user   |
| POST   | /api/auth/login       | No   | Login and get JWT     |

### Users

| Method | Endpoint              | Auth | Description                    |
|--------|-----------------------|------|--------------------------------|
| GET    | /api/users/me         | Yes  | Get current authenticated user|
| GET    | /api/users            | Yes  | List all users except self     |

### Conversations

| Method | Endpoint                    | Auth | Description                       |
|--------|-----------------------------|------|-----------------------------------|
| GET    | /api/conversations          | Yes  | List current user's conversations |
| POST   | /api/conversations/{userId} | Yes  | Create or get a conversation      |

### Messages

| Method | Endpoint                                  | Auth | Description              |
|--------|-------------------------------------------|------|--------------------------|
| GET    | /api/conversations/{conversationId}/messages | Yes | Get message history     |
| DELETE | /api/messages/{messageId}                 | Yes  | Delete own message       |

## WebSocket Endpoints

| Purpose         | Destination                          |
|-----------------|--------------------------------------|
| Connect         | /ws                                  |
| Send message    | /app/chat.send                       |
| Subscribe       | /topic/conversation/{conversationId} |
| Presence events | /topic/presence                      |

## Authentication

All protected endpoints require:

```
Authorization: Bearer <JWT_TOKEN>
```

The JWT is obtained from the login endpoint. It contains the user ID as the subject, plus email and username claims. The token expires after 24 hours by default.

## Database Structure

### users collection

```json
{
  "_id": "user123",
  "username": "amit",
  "email": "amit@example.com",
  "password": "$2a$10$...",
  "online": false,
  "lastSeen": "2026-08-13T18:30:00",
  "createdAt": "2026-08-13T18:00:00"
}
```

### conversations collection

```json
{
  "_id": "conversation123",
  "participantIds": ["user123", "user456"],
  "lastMessage": "Hello!",
  "createdAt": "2026-08-13T19:00:00",
  "updatedAt": "2026-08-13T19:30:00"
}
```

### messages collection

```json
{
  "_id": "message123",
  "conversationId": "conversation123",
  "senderId": "user123",
  "senderUsername": "amit",
  "content": "Hello Rahul",
  "createdAt": "2026-08-13T20:00:00",
  "deleted": false
}
```

## Example Requests

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"amit","email":"amit@example.com","password":"password123"}'
```

Response (201 Created):

```json
{"message": "User registered successfully"}
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"amit@example.com","password":"password123"}'
```

Response (200 OK):

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "user123",
    "username": "amit",
    "email": "amit@example.com"
  }
}
```

### Get Current User

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <JWT>"
```

Response (200 OK):

```json
{
  "id": "user123",
  "username": "amit",
  "email": "amit@example.com",
  "online": true,
  "lastSeen": null
}
```

### List Users

```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <JWT>"
```

Response (200 OK):

```json
[
  {
    "id": "user456",
    "username": "rahul",
    "email": "rahul@example.com",
    "online": true,
    "lastSeen": null
  }
]
```

### Create or Get Conversation

```bash
curl -X POST http://localhost:8080/api/conversations/user456 \
  -H "Authorization: Bearer <JWT>"
```

Response (200 OK):

```json
{
  "id": "conversation123",
  "participant": {
    "id": "user456",
    "username": "rahul",
    "online": true,
    "lastSeen": null
  },
  "lastMessage": null,
  "updatedAt": "2026-08-13T19:00:00"
}
```

### Get Conversations

```bash
curl -X GET http://localhost:8080/api/conversations \
  -H "Authorization: Bearer <JWT>"
```

### Get Message History

```bash
curl -X GET http://localhost:8080/api/conversations/conversation123/messages \
  -H "Authorization: Bearer <JWT>"
```

Response (200 OK):

```json
[
  {
    "id": "message1",
    "conversationId": "conversation123",
    "senderId": "user123",
    "senderUsername": "amit",
    "content": "Hello Rahul",
    "createdAt": "2026-08-13T19:30:00",
    "deleted": false
  }
]
```

### Delete Message

```bash
curl -X DELETE http://localhost:8080/api/messages/message1 \
  -H "Authorization: Bearer <JWT>"
```

Response: 204 No Content

## Example WebSocket Messages

### Send a Message

Connect to `ws://localhost:8080/ws` with STOMP, then send:

```
SEND destination: /app/chat.send
content-type: application/json

{
  "conversationId": "conversation123",
  "content": "Hello Rahul"
}
```

### Receive a Message

Subscribe to `/topic/conversation/conversation123`:

```json
{
  "id": "message123",
  "conversationId": "conversation123",
  "senderId": "user123",
  "senderUsername": "amit",
  "content": "Hello Rahul",
  "createdAt": "2026-08-13T20:00:00",
  "deleted": false
}
```

### Presence Events

Subscribe to `/topic/presence`:

```json
{
  "userId": "user123",
  "username": "amit",
  "online": true,
  "lastSeen": null
}
```

## Testing

```bash
mvn test
```

Tests use an embedded MongoDB (via flapdoodle) and cover authentication, authorization, conversations, and messages.
