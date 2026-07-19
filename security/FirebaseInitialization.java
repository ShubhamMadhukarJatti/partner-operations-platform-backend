package com.sharkdom.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class FirebaseInitialization {

    @Value("${firebase.firestore-url}")
    private String firestoreUrl;

    @Value("${firebase.key-file-name}")
    private String keyFileName;

    FirebaseApp firebaseApp;

    @PostConstruct
    public void initialization() {
        try {
            ClassPathResource serviceAccount = new ClassPathResource(keyFileName);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .setDatabaseUrl(firestoreUrl).build();
            firebaseApp = FirebaseApp.initializeApp(options);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}