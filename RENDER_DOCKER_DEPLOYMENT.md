# Deploy Investment API to Render using Docker

Complete step-by-step guide to deploy your Spring Boot Investment API to Render using Docker.

## Prerequisites

1. **GitHub Account** - Your code must be on GitHub
2. **Render Account** - Sign up at [render.com](https://render.com)
3. **Firebase Service Account Key** - JSON credentials file
4. **Docker** (optional, for local testing)

## Step 1: Verify Your Repository

Your code should be on GitHub at: `https://github.com/MohamedAmin19/investments.git`

Ensure these files are committed:
- ✅ `Dockerfile`
- ✅ `.dockerignore`
- ✅ `pom.xml`
- ✅ All source files
- ✅ `.gitignore` (excludes `serviceAccountKey.json`)

## Step 2: Prepare Firebase Credentials

You'll need to convert your `serviceAccountKey.json` to an environment variable:

1. Open your `serviceAccountKey.json` file
2. Copy the entire JSON content
3. Minify it to a single line (remove all line breaks and spaces)
   - Use an online tool: [jsonformatter.org/json-minify](https://jsonformatter.org/json-minify)
   - Or use command: `cat serviceAccountKey.json | jq -c`

**Example format (single line):**
```json
{"type":"service_account","project_id":"your-project","private_key_id":"...","private_key":"-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",...}
```

## Step 3: Sign Up / Login to Render

1. Go to [dashboard.render.com](https://dashboard.render.com)
2. Sign up or log in with your GitHub account
3. Authorize Render to access your GitHub repositories

## Step 4: Create a New Web Service

1. Click **"New +"** button in the dashboard
2. Select **"Web Service"**
3. Connect your GitHub account if not already connected
4. Find and select the repository: `MohamedAmin19/investments`
5. Click **"Connect"**

## Step 5: Configure Service Settings

### Basic Settings:

- **Name:** `investment-api` (or your preferred name)
- **Region:** `Oregon (US West)` or closest to your users
- **Branch:** `main` (or your default branch)
- **Root Directory:** 
  - If your code is in root: Leave empty
  - If your code is in `Investment` folder: Enter `Investment`

### Build & Deploy:

- **Environment:** Select **"Docker"** (this is what you selected in the image)
- **Dockerfile Path:** 
  - If code is in root: `Dockerfile`
  - If code is in `Investment` folder: `Investment/Dockerfile`
- **Docker Context:** 
  - If code is in root: `.` (current directory)
  - If code is in `Investment` folder: `Investment`

**⚠️ Important Configuration:**
- **Build Command:** Leave empty (Docker handles this)
- **Start Command:** Leave empty (Dockerfile ENTRYPOINT handles this)

### Instance Type:

- **Free Tier:** 512 MB RAM (apps sleep after 15 min inactivity)
- **Starter ($7/month):** 512 MB RAM, always-on
- **Standard ($25/month):** 1 GB RAM, always-on (recommended for production)

## Step 6: Add Environment Variables

Click on **"Environment"** tab and add the following variables:

### 1. FIREBASE_CREDENTIALS_JSON (Required)

- **Key:** `FIREBASE_CREDENTIALS_JSON`
- **Value:** Paste your minified `serviceAccountKey.json` content as a single-line JSON string

**How to get the value:**
1. Open `serviceAccountKey.json`
2. Copy entire content
3. Minify it (remove line breaks, make it one line)
4. Paste in Render

**Example:**
```
{"type":"service_account","project_id":"investment-e1396","private_key_id":"abc123","private_key":"-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...\n-----END PRIVATE KEY-----\n","client_email":"firebase-adminsdk@investment-e1396.iam.gserviceaccount.com","client_id":"123456789","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_x509_cert_url":"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk%40investment-e1396.iam.gserviceaccount.com"}
```

### 2. JWT_SECRET (Required)

- **Key:** `JWT_SECRET`
- **Value:** A strong random string (minimum 32 characters)

**Generate a secret:**
- Linux/Mac: `openssl rand -base64 32`
- Online: Use a password generator (32+ characters)
- Example: `aB3$kL9#mN2@pQ7&rT5^uV8*wX1!yZ4%cE6@dF7#gH8`

### 3. PORT (Auto-set by Render)

- Render automatically sets this
- Your app reads it via `${PORT:8080}` in `application.properties`
- **Don't manually set this**

### 4. SPRING_PROFILES_ACTIVE (Optional)

- **Key:** `SPRING_PROFILES_ACTIVE`
- **Value:** `production`

### 5. JWT_EXPIRATION (Optional)

- **Key:** `JWT_EXPIRATION`
- **Value:** `86400000` (24 hours in milliseconds)

## Step 7: Advanced Settings (Optional)

### Health Check Path:
- Set to: `/api/ping`
- This helps Render monitor your service health

### Auto-Deploy:
- ✅ Enabled by default
- Deploys automatically on every push to the selected branch

## Step 8: Deploy

1. Review all settings
2. Click **"Create Web Service"**
3. Render will start building your Docker image
4. Watch the build logs in real-time
5. Build process:
   - Clones your repository
   - Builds Docker image using Dockerfile
   - Runs Maven build inside container
   - Starts the Spring Boot application
6. Once deployment completes, you'll see a URL like: `https://investment-api.onrender.com`

## Step 9: Verify Deployment

### 1. Test Ping Endpoint:
```bash
curl https://your-app-name.onrender.com/api/ping
```

**Expected Response:**
```json
{
  "status": "ok",
  "message": "Service is running",
  "timestamp": 1705454325000
}
```

### 2. Test Health Endpoint:
```bash
curl https://your-app-name.onrender.com/api/health
```

### 3. Test POST Endpoint (Create Investment):
```bash
curl -X POST https://your-app-name.onrender.com/api/investments \
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

### 4. Test Auth Endpoint:
```bash
curl -X POST https://your-app-name.onrender.com/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin@123"
  }'
```

### 5. Test GET Endpoint (with token):
```bash
# First get token
TOKEN=$(curl -X POST https://your-app-name.onrender.com/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin@123"}' | jq -r '.token')

# Then use token
curl -X GET "https://your-app-name.onrender.com/api/investments?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

## Step 10: Monitor Your Deployment

### View Logs:
1. Go to your service dashboard on Render
2. Click **"Logs"** tab
3. View real-time logs and errors

### Common Log Locations:
- Build logs: Shows Docker build process
- Runtime logs: Shows Spring Boot application logs
- Error logs: Shows any runtime errors

## Troubleshooting

### Issue: Build Fails - "Cannot find Dockerfile"

**Solution:**
- Verify `Dockerfile` is in your repository
- Check the "Dockerfile Path" setting matches your repository structure
- If code is in `Investment` folder, set path to `Investment/Dockerfile`

### Issue: Build Fails - "Maven build error"

**Solution:**
- Check build logs for specific Maven errors
- Verify `pom.xml` is correct
- Ensure all dependencies are available

### Issue: "Application failed to start" - Port binding

**Solution:**
- Verify `server.port=${PORT:8080}` in `application.properties`
- Render automatically sets `PORT` environment variable
- Dockerfile should expose port 8080

### Issue: Firebase initialization fails

**Solution:**
- Verify `FIREBASE_CREDENTIALS_JSON` is set correctly
- Check that JSON is minified (single line, no line breaks)
- Verify service account has proper permissions
- Check logs for specific Firebase error messages

### Issue: "Cannot find serviceAccountKey.json"

**Solution:**
- This is expected! The app should use `FIREBASE_CREDENTIALS_JSON` environment variable
- Verify the environment variable is set in Render dashboard
- Check that `FirebaseConfig.java` reads from environment variable first

### Issue: App sleeps after inactivity (Free Tier)

**Solution:**
- Free tier apps sleep after 15 minutes of inactivity
- First request after sleep takes ~30-60 seconds (cold start)
- For always-on service, upgrade to paid tier ($7/month minimum)

### Issue: Build timeout

**Solution:**
- Free tier has build timeout limits (~15 minutes)
- Optimize Dockerfile (multi-stage build already implemented)
- Consider upgrading to paid tier for longer build times

## Docker Configuration Details

### Dockerfile Structure:
- **Stage 1 (Build):** Uses Maven to compile and package the application
- **Stage 2 (Runtime):** Uses lightweight JRE to run the application
- **Health Check:** Monitors `/api/ping` endpoint
- **Non-root user:** Runs as `spring` user for security

### .dockerignore:
Excludes unnecessary files from Docker build context:
- `target/` (build artifacts)
- `.git/` (version control)
- IDE files
- Documentation files
- `serviceAccountKey.json` (security)

## Environment Variables Summary

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `FIREBASE_CREDENTIALS_JSON` | ✅ Yes | Minified Firebase service account JSON | `{"type":"service_account",...}` |
| `JWT_SECRET` | ✅ Yes | Strong secret for JWT (32+ chars) | `aB3$kL9#mN2@pQ7&rT5^uV8` |
| `PORT` | ❌ No | Auto-set by Render | `8080` |
| `SPRING_PROFILES_ACTIVE` | ❌ No | Spring profile | `production` |
| `JWT_EXPIRATION` | ❌ No | Token expiration (ms) | `86400000` |

## Local Docker Testing (Optional)

Before deploying to Render, test locally:

```bash
# Build the image
docker build -t investment-api .

# Run the container
docker run -p 8080:8080 \
  -e FIREBASE_CREDENTIALS_JSON='{"type":"service_account",...}' \
  -e JWT_SECRET='your-secret-key' \
  investment-api

# Or use docker-compose
docker-compose up
```

## Security Best Practices

1. ✅ Never commit `serviceAccountKey.json` to Git
2. ✅ Use strong `JWT_SECRET` (generate with: `openssl rand -base64 32`)
3. ✅ Rotate secrets periodically
4. ✅ Use environment variables for all sensitive data
5. ✅ Enable HTTPS (automatic on Render)
6. ✅ Configure Firestore security rules properly
7. ✅ Monitor logs for suspicious activity
8. ✅ Use non-root user in Docker (already configured)

## Cost Considerations

### Free Tier:
- ✅ Free for testing/development
- ⚠️ Apps sleep after 15 minutes inactivity
- ⚠️ Slower cold starts (~30-60 seconds)
- ⚠️ Limited build time (~15 minutes)

### Paid Tiers:
- **Starter ($7/month):** Always-on, 512 MB RAM
- **Standard ($25/month):** Always-on, 1 GB RAM (recommended)
- **Pro ($85/month):** Always-on, 2 GB RAM, better performance

## Next Steps After Deployment

1. **Set up custom domain** (optional, paid feature)
2. **Configure auto-deploy** from GitHub (default behavior)
3. **Set up monitoring/alerts** (Render dashboard)
4. **Configure backups** for Firestore data
5. **Set up CI/CD** for automated testing
6. **Update Firestore security rules** for production

## Support Resources

- **Render Documentation:** [render.com/docs](https://render.com/docs)
- **Render Support:** [render.com/support](https://render.com/support)
- **Docker Documentation:** [docs.docker.com](https://docs.docker.com)
- **Your App URL:** Check your Render dashboard after deployment

---

**🎉 Congratulations! Your Investment API is now deployed on Render using Docker!**

Your API will be available at: `https://your-app-name.onrender.com`

