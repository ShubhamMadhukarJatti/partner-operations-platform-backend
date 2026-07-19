package com.sharkdom.controller.ai;


import com.sharkdom.service.ai.DocFetcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/docfetcher")
@RequiredArgsConstructor
public class DocFetcherController {

    private final DocFetcherService docFetcherService;

    @PostMapping(value = "/extract-agreement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractAgreement(@RequestParam("file") MultipartFile file) {
        log.debug("API request received for file: {}", file != null ? file.getOriginalFilename() : "null");
        return docFetcherService.extractAgreement(file);
    }
}
