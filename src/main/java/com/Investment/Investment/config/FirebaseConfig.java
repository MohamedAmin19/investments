package com.Investment.Investment.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.credentials.json:}")
    private String credentialsJson;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions.Builder builder = FirebaseOptions.builder();

                // Load credentials with priority: Environment variable > File path > Classpath resource
                InputStream serviceAccount = getCredentialsInputStream();

                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                builder.setCredentials(credentials);

                FirebaseOptions options = builder.build();
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }

    private InputStream getCredentialsInputStream() throws Exception {
        // Priority 1: Environment variable (FIREBASE_CREDENTIALS_JSON) - for Docker/Render
        if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
            return new ByteArrayInputStream(credentialsJson.getBytes());
        }

        // Priority 2: File path from configuration
        if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
            try {
                return new FileInputStream(credentialsPath);
            } catch (Exception e) {
                // If file path fails, try as classpath resource
                try {
                    return new ClassPathResource(credentialsPath).getInputStream();
                } catch (Exception ex) {
                    // Continue to default
                }
            }
        }

        // Priority 3: Default classpath resource
        try {
            return new ClassPathResource("serviceAccountKey.json").getInputStream();
        } catch (Exception e) {
            throw new RuntimeException(
                "Firebase credentials not found. Please set FIREBASE_CREDENTIALS_JSON environment variable " +
                "or provide serviceAccountKey.json file.", e);
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}

