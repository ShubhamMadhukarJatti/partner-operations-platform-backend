package com.sharkdom.partnerattribution.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sharkdom.partnerattribution.dto.FileUploadResponseDto;
import com.sharkdom.util.Util;
import com.sharkdom.util.aws.service.AmazonS3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    @Value("${logo.s3Path}")
    private String logoS3Path;

    private final AmazonS3Service amazonS3Service;

    private static final String BUCKET_NAME = "sharkdom.co.in";

    @Transactional
    public FileUploadResponseDto uploadFile(MultipartFile file, String folder) {

        try {
            Long orgId = Util.getOrgIdFromToken();

            var s3Client = amazonS3Service.getS3Instance();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            String fileName = buildFileName(folder, orgId, file.getOriginalFilename());

            log.info("Uploading to S3 | bucket={} | key={}", BUCKET_NAME, fileName);

            s3Client.putObject(
                    new PutObjectRequest(
                            BUCKET_NAME,
                            fileName,
                            file.getInputStream(),
                            metadata
                    )
            );

            String fileUrl = buildFileUrl(fileName);

            return new FileUploadResponseDto(fileUrl);

        } catch (Exception e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Unable to upload file");
        }
    }

    private String buildFileName(String folder, Long orgId, String originalName) {
        return folder + "/" + orgId + "/" +
                System.currentTimeMillis() + "_" + originalName;
    }

    private String buildFileUrl(String fileName) {
        return "https://s3.ap-south-1.amazonaws.com/" + BUCKET_NAME + "/" + fileName;
    }
}