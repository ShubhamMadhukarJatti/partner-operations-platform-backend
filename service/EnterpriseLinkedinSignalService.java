package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseLinkedinSignalService {

    private final RestTemplate restTemplate;

    @Value("${outreach.base-url}")
    private String baseUrl;

    @Value("${outreach.api-key}")
    private String apiKey;

    // ================= HANDLE INSUFFICIENT SIGNAL =================
    public EnterpriseLinkedinInsufficientSignalResponse handleInsufficientSignal(EnterpriseLinkedinInsufficientSignalRequest req){
        String url=baseUrl+"/outreach/generate"; // build URL

        // prepare headers
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); headers.set("X-API-Key",apiKey);

        HttpEntity<EnterpriseLinkedinInsufficientSignalRequest> entity=new HttpEntity<>(req,headers);

        try{
            // call external API
            ResponseEntity<EnterpriseLinkedinInsufficientSignalResponse> res=restTemplate.exchange(url,HttpMethod.POST,entity,EnterpriseLinkedinInsufficientSignalResponse.class);
            EnterpriseLinkedinInsufficientSignalResponse body=res.getBody();

            // validate response
            if(!res.getStatusCode().is2xxSuccessful()||body==null){
                log.error("Signal API failed. status={}",res.getStatusCode());
                throw new RestClientException("Failed to handle insufficient signal");
            }

            // guardrail handling
            if(Boolean.TRUE.equals(body.getGuardrailTriggered()))
                log.warn("AI Guardrail Triggered | reason={}",body.getGuardrailReason());

            return body;

        }catch(RestClientException ex){
            log.error("Error calling Signal API. msg={}",ex.getMessage(),ex);
            throw ex; // or wrap with ServiceException
        }
    }
}