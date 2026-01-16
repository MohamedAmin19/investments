# Postman Testing Guide for Investment API

## Prerequisites

1. **Application is running** on `http://localhost:8080`
2. **Postman** installed on your system
3. **Firebase is configured** with `serviceAccountKey.json` in place

---

## Step-by-Step Testing Instructions

### **Step 1: Generate JWT Token**

This is the first step to get a token for accessing protected GET endpoints.

1. **Open Postman** and create a new request
2. **Method:** `POST`
3. **URL:** `http://localhost:8080/api/auth/token`
4. **Headers:**
   - Key: `Content-Type`
   - Value: `application/json`
5. **Body:** 
   - Select **`raw`** and **`JSON`** from the dropdown
   - Enter the following JSON:
     ```json
     {
       "username": "test-user"
     }
     ```
6. **Click "Send"**
7. **Expected Response (200 OK):**
   ```json
   {
     "success": true,
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "tokenType": "Bearer"
   }
   ```
8. **Copy the `token` value** - you'll need it for Step 3 and 4!

---

### **Step 2: Create Investment Profile (POST - No Auth Required)**

1. **Create a new request** in Postman
2. **Method:** `POST`
3. **URL:** `http://localhost:8080/api/investments`
4. **Headers:**
   - Key: `Content-Type`
   - Value: `application/json`
5. **Body:**
   - Select **`raw`** and **`JSON`**
   - Enter the following JSON:
     ```json
     {
       "firstName": "John",
       "middleName": "Michael",
       "lastName": "Doe",
       "age": 30,
       "mobileNumber": "+1234567890",
       "emailAddress": "john.doe@example.com",
       "profession": "Engineer",
       "investmentBackground": "Beginner",
       "currentInvestments": ["Stocks", "Bonds", "Mutual Funds"],
       "mostInterestedIn": "Long-term growth"
     }
     ```
6. **Click "Send"**
7. **Expected Response (201 Created):**
   ```json
   {
     "success": true,
     "message": "Investment data saved successfully",
     "id": "-N1234567890abcdef"
   }
   ```
8. **Save the `id` value** - you'll need it for Step 4!

---

### **Step 2b: Create Investment with "Other" Profession (Optional Test)**

Test the conditional `professionOther` field:

1. **Create a new request** in Postman
2. **Method:** `POST`
3. **URL:** `http://localhost:8080/api/investments`
4. **Headers:**
   - Key: `Content-Type`
   - Value: `application/json`
5. **Body:**
   ```json
   {
     "firstName": "Jane",
     "lastName": "Smith",
     "age": 35,
     "mobileNumber": "9876543210",
     "emailAddress": "jane.smith@example.com",
     "profession": "Other",
     "professionOther": "Freelance Designer",
     "investmentBackground": "Intermediate",
     "currentInvestments": ["Real Estate", "Cryptocurrency"],
     "mostInterestedIn": "Diversification"
   }
   ```
6. **Click "Send"**
7. **Expected Response (201 Created):** Same as Step 2

---

### **Step 3: Get All Investment Profiles (GET - Auth Required)**

1. **Create a new request** in Postman
2. **Method:** `GET`
3. **URL:** `http://localhost:8080/api/investments`
4. **Headers:**
   - Key: `Authorization`
   - Value: `Bearer YOUR_TOKEN_FROM_STEP_1` 
     - Replace `YOUR_TOKEN_FROM_STEP_1` with the actual token you copied in Step 1
     - Example: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
5. **Click "Send"**
6. **Expected Response (200 OK):**
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

**Note:** If you get a **401 Unauthorized** error, check that:
- Your token is valid (not expired)
- You included "Bearer " before the token
- There's a space between "Bearer" and the token

---

### **Step 4: Get Investment Profile by ID (GET - Auth Required)**

1. **Create a new request** in Postman
2. **Method:** `GET`
3. **URL:** `http://localhost:8080/api/investments/{id}`
   - Replace `{id}` with the actual ID you got from Step 2
   - Example: `http://localhost:8080/api/investments/-N1234567890abcdef`
4. **Headers:**
   - Key: `Authorization`
   - Value: `Bearer YOUR_TOKEN_FROM_STEP_1`
5. **Click "Send"**
6. **Expected Response (200 OK):**
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

---

## Testing Error Scenarios

### **Test 1: Missing Required Fields**

**POST** `http://localhost:8080/api/investments`

```json
{
  "firstName": "John",
  "age": 30
}
```

**Expected Response (400 Bad Request):** Validation errors

---

### **Test 2: Invalid Email Format**

**POST** `http://localhost:8080/api/investments`

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "age": 30,
  "mobileNumber": "1234567890",
  "emailAddress": "invalid-email",
  "profession": "Engineer",
  "investmentBackground": "Beginner",
  "currentInvestments": ["Stocks"],
  "mostInterestedIn": "Growth"
}
```

**Expected Response (400 Bad Request):** Email validation error

---

### **Test 3: "Other" Profession Without professionOther Field**

**POST** `http://localhost:8080/api/investments`

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "age": 30,
  "mobileNumber": "1234567890",
  "emailAddress": "john@example.com",
  "profession": "Other",
  "investmentBackground": "Beginner",
  "currentInvestments": ["Stocks"],
  "mostInterestedIn": "Growth"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "professionOther is required when profession is 'Other'",
  "message": "If profession is 'Other', you must provide a value for professionOther"
}
```

---

### **Test 4: GET Without Token (Unauthorized)**

**GET** `http://localhost:8080/api/investments` (no Authorization header)

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2024-01-16T00:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

---

### **Test 5: GET With Invalid Token**

**GET** `http://localhost:8080/api/investments`

**Headers:**
- `Authorization: Bearer invalid-token-12345`

**Expected Response (401 Unauthorized):** Authentication error

---

### **Test 6: GET Non-Existent Investment**

**GET** `http://localhost:8080/api/investments/non-existent-id`

**Headers:**
- `Authorization: Bearer YOUR_VALID_TOKEN`

**Expected Response (404 Not Found):**
```json
{
  "success": false,
  "error": "Investment not found"
}
```

---

## Creating a Postman Collection (Optional)

### **Quick Setup:**

1. In Postman, click **"New"** → **"Collection"**
2. Name it: `Investment API`
3. Add all 4 requests to this collection:
   - **Generate Token** (POST `/api/auth/token`)
   - **Create Investment** (POST `/api/investments`)
   - **Get All Investments** (GET `/api/investments`)
   - **Get Investment by ID** (GET `/api/investments/{id}`)

### **Using Variables in Postman:**

1. Go to **Collection** → **Variables**
2. Add variables:
   - `base_url`: `http://localhost:8080`
   - `token`: (leave empty, set after Step 1)
3. Update URLs to use: `{{base_url}}/api/investments`
4. Update Authorization header to use: `Bearer {{token}}`

### **Automating Token Setup:**

1. After Step 1 (Generate Token), go to the **Tests** tab
2. Add this script:
   ```javascript
   if (pm.response.code === 200) {
       var jsonData = pm.response.json();
       pm.collectionVariables.set("token", jsonData.token);
   }
   ```
3. Now, all requests in the collection will automatically use the token!

---

## Troubleshooting

### **Issue: "Connection refused" or "Cannot connect"**

**Solution:**
- Make sure the Spring Boot application is running
- Check the console for any startup errors
- Verify the port is `8080` (check `application.properties`)

### **Issue: "Failed to initialize Firebase"**

**Solution:**
- Check that `serviceAccountKey.json` exists in `src/main/resources/`
- Verify the Firebase database URL in `application.properties`
- Check the console logs for specific error messages

### **Issue: "401 Unauthorized" on GET requests**

**Solution:**
- Make sure you generated a token first (Step 1)
- Verify the Authorization header format: `Bearer <token>` (with space)
- Check if the token has expired (default: 24 hours)
- Generate a new token if needed

### **Issue: "404 Not Found" on endpoints**

**Solution:**
- Verify the base URL is correct: `http://localhost:8080`
- Check the endpoint paths (they start with `/api/`)
- Make sure the application started successfully

---

## Quick Reference

| Endpoint | Method | Auth Required | Purpose |
|----------|--------|---------------|---------|
| `/api/auth/token` | POST | No | Generate JWT token |
| `/api/investments` | POST | No | Create investment profile |
| `/api/investments` | GET | Yes | Get all investments |
| `/api/investments/{id}` | GET | Yes | Get investment by ID |

---

## Success Checklist

- ✅ Application is running on port 8080
- ✅ Can generate JWT token
- ✅ Can create investment profile (POST)
- ✅ Can create investment with "Other" profession
- ✅ Can get all investments (GET with token)
- ✅ Can get specific investment by ID (GET with token)
- ✅ Error handling works (invalid data, missing token, etc.)

---

**Happy Testing! 🚀**

