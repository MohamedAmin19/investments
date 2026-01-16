# Firestore Setup Guide

Follow these steps to set up Firebase Firestore for your Investment Application:

## Step 1: Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** or **"Create a project"**
3. Enter your project name (e.g., "Investment App")
4. Click **Continue**
5. (Optional) Enable Google Analytics if needed
6. Click **Create project**
7. Wait for the project to be created, then click **Continue**

## Step 2: Enable Firestore Database

1. In your Firebase project, click on **"Firestore Database"** in the left sidebar
2. Click **"Create database"**
3. Choose **"Start in production mode"** (we'll configure security rules later)
   - **Note:** For development, you can choose "Start in test mode" but it's less secure
4. Select a **location** for your database (choose the closest to your users)
   - **Important:** Once set, you cannot change the location
5. Click **"Enable"**
6. Wait for Firestore to be created (this may take a minute)

## Step 3: Configure Firestore Security Rules

1. In Firestore Database, go to the **"Rules"** tab
2. For **development/testing**, you can use these rules:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Allow read/write access to investments collection
       match /investments/{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```
3. Click **"Publish"** to save the rules

   **⚠️ IMPORTANT:** The above rules allow anyone to read/write. For production, use proper authentication-based rules:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /investments/{document=**} {
         // Only authenticated users can read
         allow read: if request.auth != null;
         // Anyone can write (for POST endpoint)
         allow write: if true;
       }
     }
   }
   ```

## Step 4: Generate Service Account Key

1. Click on the **gear icon** ⚙️ next to "Project Overview" in the left sidebar
2. Select **"Project settings"**
3. Go to the **"Service accounts"** tab
4. Click **"Generate new private key"**
5. A confirmation dialog will appear - click **"Generate key"**
6. A JSON file will be downloaded - this is your service account key
7. **Rename this file to `serviceAccountKey.json`**
8. **Place this file in:** `Investment/src/main/resources/serviceAccountKey.json`

⚠️ **IMPORTANT SECURITY NOTE:**
- Never commit `serviceAccountKey.json` to version control (Git)
- Add it to `.gitignore` file (already done)
- Keep this file secure - it has admin access to your Firebase project
- In production, use environment variables or a secure secret management system

## Step 5: Verify Service Account Permissions

The service account key you downloaded should have the **"Cloud Datastore User"** or **"Firebase Admin"** role. This is usually set by default when you generate the key from Firebase Console.

If you need to check or modify permissions:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Go to **IAM & Admin** → **IAM**
4. Find your service account (it will have an email like `firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com`)
5. Verify it has the necessary permissions

## Step 6: Update application.properties

1. Open `Investment/src/main/resources/application.properties`
2. Verify the configuration:
   ```properties
   firebase.credentials.path=serviceAccountKey.json
   ```
   - The `firebase.database.url` is **NOT needed** for Firestore (removed)
   - The credentials path should point to your `serviceAccountKey.json` file

## Step 7: Test Your Setup

1. Start your Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

2. Test the POST endpoint (creates a document in Firestore):
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

3. Check Firestore Console:
   - Go to **Firestore Database** → **Data** tab
   - You should see a new collection called **"investments"**
   - Click on it to see the document you just created

4. Test the GET endpoint (requires token):
   ```bash
   # First, get a token
   curl -X POST http://localhost:8080/api/auth/token \
     -H "Content-Type: application/json" \
     -d '{"username": "test-user"}'
   
   # Then, use the token to get all investments
   curl -X GET http://localhost:8080/api/investments \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

## Step 8: Understanding Firestore Structure

Your data will be stored in Firestore with this structure:

```
investments (collection)
  └── {document-id} (document)
      ├── firstName: "John"
      ├── lastName: "Doe"
      ├── age: 30
      ├── mobileNumber: "1234567890"
      ├── emailAddress: "john@example.com"
      ├── profession: "Engineer"
      ├── professionOther: null
      ├── investmentBackground: "Beginner"
      ├── currentInvestments: ["Stocks", "Bonds"]
      ├── mostInterestedIn: "Long-term growth"
      ├── createdAt: 1234567890000
      └── updatedAt: 1234567890000
```

## Troubleshooting

### Issue: "Failed to initialize Firebase"

**Possible causes:**
- `serviceAccountKey.json` file is missing or in wrong location
- JSON file is corrupted or invalid
- File path in `application.properties` is incorrect

**Solution:**
- Verify the file exists at `src/main/resources/serviceAccountKey.json`
- Check the JSON file is valid (open it and verify it's proper JSON)
- Verify the file path in `application.properties`

### Issue: "Permission denied" errors

**Possible causes:**
- Service account doesn't have proper permissions
- Firestore security rules are too restrictive
- Wrong project selected

**Solution:**
- Check service account permissions in Google Cloud Console
- Review Firestore security rules
- Verify you're using the correct service account key for your project

### Issue: "Collection not found" or "Document not found"

**Possible causes:**
- Collection name mismatch
- Document doesn't exist
- Firestore not properly initialized

**Solution:**
- Check the collection name in code matches Firestore
- Verify documents exist in Firestore Console
- Check application logs for initialization errors

### Issue: Application starts but can't connect to Firestore

**Possible causes:**
- Network/firewall issues
- Service account key is for a different project
- Firestore not enabled in Firebase project

**Solution:**
- Verify Firestore is enabled in Firebase Console
- Check network connectivity
- Ensure service account key matches your Firebase project

## Firestore vs Realtime Database

**Key Differences:**
- **Firestore**: Document-based NoSQL database, better for complex queries, scales automatically
- **Realtime Database**: JSON tree-based, better for real-time synchronization

**Why Firestore for this project:**
- Better querying capabilities
- More structured data model
- Better scalability
- More modern and actively developed

## Security Best Practices

1. ✅ **Never commit `serviceAccountKey.json` to Git** (already in `.gitignore`)
2. ✅ **Use environment variables** for credentials in production
3. ✅ **Set up proper Firestore security rules** based on authentication
4. ✅ **Rotate service account keys** periodically
5. ✅ **Use least privilege principle** - only grant necessary permissions
6. ✅ **Monitor Firestore usage** and set up billing alerts
7. ✅ **Enable Firestore audit logs** for production

## Production Checklist

Before deploying to production:

- [ ] Service account key is stored securely (not in code)
- [ ] Firestore security rules are properly configured
- [ ] Billing alerts are set up
- [ ] Firestore indexes are created (if using complex queries)
- [ ] Error handling and logging are in place
- [ ] Backup strategy is defined
- [ ] Monitoring and alerts are configured

## Next Steps

- Set up Firestore indexes if you plan to query by specific fields
- Configure Firestore backup and restore
- Set up monitoring and alerts
- Implement pagination for large datasets
- Add data validation at the Firestore rules level

---

**Your Firestore setup is complete! 🎉**

The application will now use Firestore to store and retrieve investment data. All API endpoints work the same way, but data is now stored in Firestore instead of Realtime Database.

