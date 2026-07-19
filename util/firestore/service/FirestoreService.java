package com.sharkdom.util.firestore.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class FirestoreService {

    static Firestore db;

    public static void saveAsync(String collection, String document, Object data)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = getDb().collection(collection).document(document);
        ApiFuture<WriteResult> result = docRef.set(data);
        log.info("Firestore update time for document {collection}.{document}: " + result.get().getUpdateTime(),
                collection, document);
    }

    public static Firestore getDb() {
        if (!FirebaseApp.getApps().isEmpty()) {
            if (null == db) {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                db = FirestoreClient.getFirestore();
            }
        }
        return db;
    }
}
