package com.sharkdom.util.aws.config;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.sharkdom.util.firestore.service.FirestoreService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AwsConfigsProvider {

    static Map<String, String> sesConfigs = new HashMap<>();

    static Map<String, String> s3Configs = new HashMap<>();

    public static Map<String, String> getS3Configs() throws InterruptedException, ExecutionException {
        if (s3Configs.isEmpty()) {
            DocumentReference docRef = FirestoreService.getDb().collection("Configurations").document("s3Configs");
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                s3Configs.clear();
                document.getData().forEach((k, v) -> s3Configs.put(k, (String) v));
            } else {
                throw new RuntimeException("Cannot find s3Configs on firestore!");
            }
        }
        return s3Configs;
    }

    public static Map<String, String> getSesConfigs() throws InterruptedException, ExecutionException {
        if (sesConfigs.isEmpty()) {
            DocumentReference docRef = FirestoreService.getDb().collection("Configurations").document("sesConfigs");
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                sesConfigs.clear();
                document.getData().forEach((k, v) -> sesConfigs.put(k, (String) v));
            } else {
                throw new RuntimeException("Cannot find sesConfigs on firestore!");
            }
        }
        return sesConfigs;
    }
}
