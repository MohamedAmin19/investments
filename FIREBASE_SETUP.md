# Firebase Setup Guide

Follow these steps to set up Firebase for your Investment Application:

## Step 1: Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** or **"Create a project"**
3. Enter your project name (e.g., "Investment App")
4. Click **Continue**
5. (Optional) Enable Google Analytics if needed
6. Click **Create project**
7. Wait for the project to be created, then click **Continue**

## Step 2: Enable Realtime Database

1. In your Firebase project, click on **"Realtime Database"** in the left sidebar
2. Click **"Create Database"**
3. Select a location for your database (choose the closest to your users)
4. Choose **"Start in test mode"** (for development) or set up security rules later
5. Click **"Enable"**

## Step 3: Get Your Database URL

1. After creating the database, you'll see your database URL
2. It will look like: `https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/`
3. Copy this URL - you'll need it for `application.properties`

## Step 4: Generate Service Account Key

1. Click on the **gear icon** ⚙️ next to "Project Overview" in the left sidebar
2. Select **"Project settings"**
3. Go to the **"Service accounts"** tab
4. Click **"Generate new private key"**
5. A JSON file will be downloaded - this is your service account key
6. **Rename this file to `serviceAccountKey.json`**
7. **Place this file in:** `Investment/src/main/resources/serviceAccountKey.json`

⚠️ **IMPORTANT SECURITY NOTE:**
- Never commit `serviceAccountKey.json` to version control (Git)
- Add it to `.gitignore` file
- Keep this file secure - it has admin access to your Firebase project

## Step 5: Update application.properties

1. Open `Investment/src/main/resources/application.properties`
2. Update the `firebase.database.url` with your actual database URL:
   ```
   firebase.database.url=https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/
   ```

## Step 6: Configure Security Rules (Important!)

1. Go to **Realtime Database** → **Rules** tab in Firebase Console
2. For development, you can use test mode initially:
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```
3. **For production, use proper security rules:**
   ```json
   {
     "rules": {
       "investments": {
         ".read": false,
         ".write": true,
         "$investmentId": {
           ".read": false
         }
       }
     }
   }
   ```
   Note: The security is handled by JWT tokens in your Spring Boot application, so Firebase rules can be more restrictive.

## Step 7: (Optional) Change JWT Secret

1. Open `Investment/src/main/resources/application.properties`
2. Change `jwt.secret` to a strong random string (at least 32 characters):
   ```
   jwt.secret=your-very-long-and-random-secret-key-for-production
   ```

## Step 8: Test Your Setup

1. Start your Spring Boot application
2. Test the POST endpoint (no token required):
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

3. Get a token:
   ```bash
   curl -X POST http://localhost:8080/api/auth/token \
     -H "Content-Type: application/json" \
     -d '{"username": "test-user"}'
   ```

4. Test the GET endpoint (token required):
   ```bash
   curl -X GET http://localhost:8080/api/investments \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

## Troubleshooting

### Issue: "Failed to initialize Firebase"
- **Solution:** Make sure `serviceAccountKey.json` is in `src/main/resources/` directory
- Check that the JSON file is valid
- Verify the file path in `application.properties` if you're using a custom path

### Issue: "Permission denied" errors
- **Solution:** Check your Firebase Security Rules
- Make sure your service account has proper permissions
- Verify the database URL is correct

### Issue: "Database URL not found"
- **Solution:** Make sure you've enabled Realtime Database (not Firestore)
- Verify the URL format in `application.properties`

## Security Best Practices

1. ✅ Never commit `serviceAccountKey.json` to Git
2. ✅ Use environment variables for sensitive data in production
3. ✅ Rotate service account keys periodically
4. ✅ Set up proper Firebase Security Rules
5. ✅ Use a strong JWT secret in production
6. ✅ Enable HTTPS only in production
7. ✅ Monitor Firebase usage and set up billing alerts

## Next Steps

- Set up Firebase Authentication if you want user-based authentication
- Configure Firebase Cloud Messaging if you need push notifications
- Set up Firebase Storage if you need file uploads
- Implement proper user management for token generation

