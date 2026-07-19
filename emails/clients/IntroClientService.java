package com.sharkdom.partnerattribution.emails.clients;

import com.sharkdom.partnerattribution.emails.dto.IntroGenerateRequest;
import com.sharkdom.partnerattribution.emails.dto.response.IntroGenerateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class IntroClientService {

    private final RestTemplate restTemplate;

    public IntroGenerateResponse callExternalApi(IntroGenerateRequest request) {
        return restTemplate.postForObject(
                "https://sharkdom-intro-api-gfbkdeaqega4hkbj.centralindia-01.azurewebsites.net/generate",
                request,
                IntroGenerateResponse.class
        );
    }
}
