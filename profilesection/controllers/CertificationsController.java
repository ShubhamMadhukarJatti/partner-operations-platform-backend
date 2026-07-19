package com.sharkdom.profilesection.controllers;

import com.sharkdom.partnertraining.dto.CoverImageUploadResponseDto;
import com.sharkdom.partnertraining.service.CourseService;
import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.service.CertificationsService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.SharkdomPaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/admin/certifications")
@RequiredArgsConstructor
@Tag(name = "Admin Certification APIs", description = "Manage certifications (Admin only)")
public class CertificationsController {

    private final CertificationsService service;
    private final CourseService courseService;

    @Operation(summary = "Create Certification")
    @PostMapping
    public SharkdomApiResponse<CertificationResponse> create(
            @Valid @RequestBody CertificationRequest request) {

        return new SharkdomApiResponse<>(
                true,
                "Certification created successfully",
                service.create(request)
        );
    }

    @Operation(summary = "Update Certification")
    @PutMapping("/{id}")
    public SharkdomApiResponse<CertificationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CertificationRequest request) {

        return new SharkdomApiResponse<>(
                true,
                "Certification updated successfully",
                service.update(id, request)
        );
    }

    @Operation(summary = "Delete Certification")
    @DeleteMapping("/{id}")
    public SharkdomApiResponse<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return new SharkdomApiResponse<>(
                true,
                "Certification deleted successfully",
                null
        );
    }

    @Operation(summary = "Get Certification By Id")
    @GetMapping("/{id}")
    public SharkdomApiResponse<CertificationResponse> getById(@PathVariable Long id) {

        return new SharkdomApiResponse<>(
                true,
                "Certification fetched successfully",
                service.getById(id)
        );
    }

    @Operation(summary = "Get All Certifications (Paginated)")
    @GetMapping
    public SharkdomApiResponse<SharkdomPaginatedResponse<CertificationResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return new SharkdomApiResponse<>(
                true,
                "Certifications fetched successfully",
                service.getAll(page, size)
        );
    }

    @Operation(
            summary = "Upload Certificate",
            description = "Uploads a file to S3 and returns the public file URL"
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SharkdomApiResponse<CoverImageUploadResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading file: name={}, size={}",
                file.getOriginalFilename(), file.getSize());
        CoverImageUploadResponseDto response =
                courseService.uploadFile(file);
        log.info("File uploaded successfully. URL={}", response.getFileUrl());
        return new SharkdomApiResponse<>(
                true,
                "File uploaded successfully",
                response
        );
    }
}