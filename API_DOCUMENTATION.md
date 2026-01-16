# Investment API Documentation

## Overview

This API provides endpoints to create and retrieve investment profile data. The POST endpoint is public, while the GET endpoints require JWT authentication.

## Base URL

```
http://localhost:8080/api
```

## Authentication

GET endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

To get a token, use the auth endpoint (see below).

---

## Endpoints

### 1. Generate JWT Token

**POST** `/api/auth/token`

Generate a JWT token for authentication.

**Request Body:**
```json
{
  "username": "your-username"
}
```

**Response:**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "test-user"}'
```

---

### 2. Create Investment Profile

**POST** `/api/investments`

Create a new investment profile. This endpoint is **public** (no authentication required).

**Request Body:**
```json
{
  "firstName": "John",
  "middleName": "Michael",
  "lastName": "Doe",
  "age": 30,
  "mobileNumber": "+1234567890",
  "emailAddress": "john.doe@example.com",
  "profession": "Engineer",
  "professionOther": null,
  "investmentBackground": "Beginner",
  "currentInvestments": ["Stocks", "Bonds", "Mutual Funds"],
  "mostInterestedIn": "Long-term growth"
}
```

**Note:** If `profession` is `"Other"`, then `professionOther` field is **required**.

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Investment data saved successfully",
  "id": "-N1234567890abcdef"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "professionOther is required when profession is 'Other'",
  "message": "If profession is 'Other', you must provide a value for professionOther"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/investments \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "age": 30,
    "mobileNumber": "1234567890",
    "emailAddress": "john@example.com",
    "profession": "Engineer",
    "investmentBackground": "Beginner",
    "currentInvestments": ["Stocks", "Bonds"],
    "mostInterestedIn": "Long-term growth"
  }'
```

**Example with "Other" profession:**
```bash
curl -X POST http://localhost:8080/api/investments \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "age": 35,
    "mobileNumber": "9876543210",
    "emailAddress": "jane@example.com",
    "profession": "Other",
    "professionOther": "Freelance Designer",
    "investmentBackground": "Intermediate",
    "currentInvestments": ["Real Estate", "Cryptocurrency"],
    "mostInterestedIn": "Diversification"
  }'
```

---

### 3. Get All Investment Profiles

**GET** `/api/investments`

Retrieve all investment profiles. **Requires authentication token.**

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": "-N1234567890abcdef",
      "firstName": "John",
      "middleName": "Michael",
      "lastName": "Doe",
      "age": 30,
      "mobileNumber": "+1234567890",
      "emailAddress": "john.doe@example.com",
      "profession": "Engineer",
      "professionOther": null,
      "investmentBackground": "Beginner",
      "currentInvestments": ["Stocks", "Bonds", "Mutual Funds"],
      "mostInterestedIn": "Long-term growth",
      "createdAt": 1234567890000,
      "updatedAt": 1234567890000
    }
  ],
  "count": 1
}
```

**Example:**
```bash
# First, get a token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin"}' | jq -r '.token')

# Then, use the token to get all investments
curl -X GET http://localhost:8080/api/investments \
  -H "Authorization: Bearer $TOKEN"
```

---

### 4. Get Investment Profile by ID

**GET** `/api/investments/{id}`

Retrieve a specific investment profile by ID. **Requires authentication token.**

**Path Parameters:**
- `id` (string): The ID of the investment profile

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "-N1234567890abcdef",
    "firstName": "John",
    "middleName": "Michael",
    "lastName": "Doe",
    "age": 30,
    "mobileNumber": "+1234567890",
    "emailAddress": "john.doe@example.com",
    "profession": "Engineer",
    "professionOther": null,
    "investmentBackground": "Beginner",
    "currentInvestments": ["Stocks", "Bonds", "Mutual Funds"],
    "mostInterestedIn": "Long-term growth",
    "createdAt": 1234567890000,
    "updatedAt": 1234567890000
  }
}
```

**Not Found Response (404):**
```json
{
  "success": false,
  "error": "Investment not found"
}
```

**Example:**
```bash
curl -X GET http://localhost:8080/api/investments/-N1234567890abcdef \
  -H "Authorization: Bearer $TOKEN"
```

---

## Data Model

### Investment Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `firstName` | string | Yes | First name |
| `middleName` | string | No | Middle name |
| `lastName` | string | Yes | Last name |
| `age` | integer | Yes | Age (1-150) |
| `mobileNumber` | string | Yes | Mobile number (10-15 digits, may include +) |
| `emailAddress` | string | Yes | Valid email address |
| `profession` | string | Yes | Profession (if "Other", `professionOther` required) |
| `professionOther` | string | Conditional | Required if `profession` is "Other" |
| `investmentBackground` | string | Yes | Investment background/experience level |
| `currentInvestments` | array[string] | Yes | List of current investments (at least one required) |
| `mostInterestedIn` | string | Yes | What the user is most interested in |

### Investment Response Fields

All request fields plus:
- `id` (string): Unique identifier generated by Firebase
- `createdAt` (long): Timestamp in milliseconds
- `updatedAt` (long): Timestamp in milliseconds

---

## Error Handling

### Validation Errors (400 Bad Request)

When required fields are missing or invalid:
```json
{
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "emailAddress",
      "message": "Email must be valid"
    }
  ]
}
```

### Authentication Errors (401 Unauthorized)

When token is missing or invalid:
```json
{
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### Server Errors (500 Internal Server Error)

```json
{
  "success": false,
  "error": "Failed to save investment data",
  "message": "Error details..."
}
```

---

## Testing

### Using cURL

See examples above for each endpoint.

### Using Postman

1. **Generate Token:**
   - Method: POST
   - URL: `http://localhost:8080/api/auth/token`
   - Body (raw JSON):
     ```json
     {
       "username": "test-user"
     }
     ```

2. **Create Investment (No Auth):**
   - Method: POST
   - URL: `http://localhost:8080/api/investments`
   - Body (raw JSON): See example above

3. **Get All Investments (With Auth):**
   - Method: GET
   - URL: `http://localhost:8080/api/investments`
   - Headers:
     - Key: `Authorization`
     - Value: `Bearer YOUR_TOKEN_HERE`

---

## Notes

- The JWT token expires after 24 hours by default (configurable in `application.properties`)
- POST endpoint (`/api/investments`) does not require authentication
- GET endpoints (`/api/investments` and `/api/investments/{id}`) require valid JWT token
- All timestamps are in milliseconds since epoch (Unix timestamp)
- Firebase automatically generates unique IDs for each investment profile

