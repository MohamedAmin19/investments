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
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions.Builder builder = FirebaseOptions.builder();

                // Load credentials from file path or classpath
                InputStream serviceAccount;
                if (credentialsPath != null && !credentialsPath.isEmpty()) {
                    // Try as file path first
                    try {
                        serviceAccount = new FileInputStream(credentialsPath);
                    } catch (Exception e) {
                        // If file path fails, try as classpath resource
                        serviceAccount = new ClassPathResource(credentialsPath).getInputStream();
                    }
                } else {
                    // Default to serviceAccountKey.json in resources
                    serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream();
                }

                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                builder.setCredentials(credentials);

                FirebaseOptions options = builder.build();
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}

