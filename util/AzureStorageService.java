package com.sharkdom.util;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class AzureStorageService {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public AzureStorageService(@Value("${azure.storage.connection-string}") String connectionString,
                               @Value("${azure.storage.container-name}") String containerName) {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.containerName = containerName;
    }

    public String uploadFile(InputStream inputStream, String fileName) throws IOException {
        // Create BlobClient for the file
        BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(fileName);

        // Upload file to blob storage
        blobClient.upload(inputStream, inputStream.available(), true);

        // Optionally set content type or other headers here
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType("application/pdf"));
        return blobClient.getBlobUrl();
    }
}
