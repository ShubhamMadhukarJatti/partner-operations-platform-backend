package com.sharkdom.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.sharkdom.util.DocFetcherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class DocFetcherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL =
            "https://sharkdomdocfetcher-cjdscthdeyepfkdd.centralindia-01.azurewebsites.net/extract_agreement";

    public ResponseEntity<?> extractAgreement(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Received invalid file: null or empty");
            return ResponseEntity.badRequest().body("File is missing or empty");
        }

        try {
            log.info("Sending file [{}] ({} bytes) to DocFetcher API",
                    file.getOriginalFilename(), file.getSize());

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Wrap file as resource
            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileAsResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            DocFetcherResponse customResponse = new DocFetcherResponse();
            customResponse.setStatus("success");
            customResponse.setData(response.getBody());

            return ResponseEntity.status(response.getStatusCode()).body(customResponse);
        } catch (HttpStatusCodeException ex) {
            log.error("External API returned error: status={} response={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return ResponseEntity.status(ex.getStatusCode())
                    .body("DocFetcher API error: " + ex.getResponseBodyAsString());

        } catch (ResourceAccessException ex) {
            log.error("Failed to connect to DocFetcher API: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body("Unable to reach DocFetcher API: " + ex.getMessage());

        } catch (Exception ex) {
            log.error("Unexpected error while extracting agreement", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + ex.getMessage());
        }
    }
}
