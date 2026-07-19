package com.sharkdom.security;

import com.sharkdom.dto.IpInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class IpInfoService {

    private final RestTemplate restTemplate;

    @Value("${ipinfo.base-url}")
    private String baseUrl;

    @Value("${ipinfo.token}")
    private String token;

    public IpInfoResponse getIpDetails(String ip) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = baseUrl + "/" + ip;

        ResponseEntity<IpInfoResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                IpInfoResponse.class
        );

        return response.getBody();
    }
}