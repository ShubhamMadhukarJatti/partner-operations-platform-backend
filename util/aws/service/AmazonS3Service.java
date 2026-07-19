package com.sharkdom.util.aws.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.sharkdom.util.aws.config.AwsConfigsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class AmazonS3Service {

    public InputStream downloadFileFromS3(String filename) throws ExecutionException, InterruptedException {
        String bucketName = AwsConfigsProvider.getS3Configs().get("bucketName");
        System.out.println("Attempting to download file: " + filename + " from bucket: " + bucketName);

        // Validate bucket name and filename are not null or empty
        if (bucketName == null || bucketName.isEmpty() || filename == null || filename.isEmpty()) {
           log.error("Bucket name or filename is empty.");
        }

        // Check if file exists
        AmazonS3 s3Client = getS3Instance();
        if (!s3Client.doesObjectExist(bucketName, filename)) {
            log.error("The specified key does not exist: " + filename);
        }

        // Download the file
        return s3Client.getObject(bucketName, filename).getObjectContent();
    }

    public AmazonS3 getS3Instance() throws ExecutionException, InterruptedException {
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTH_1)  // Ensure this region is correct
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                AwsConfigsProvider.getS3Configs().get("accessKey"),
                                AwsConfigsProvider.getS3Configs().get("secretKey"))))
                .build();
    }

}
