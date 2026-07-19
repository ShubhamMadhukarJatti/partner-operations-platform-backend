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
public class EnterpriseLinkedinMessageService {

    private final RestTemplate restTemplate;

    @Value("${outreach.base-url}")
    private String baseUrl;

    @Value("${outreach.api-key}")
    private String apiKey;

    // ================= GENERATE LINKEDIN MESSAGE =================
    public EnterpriseLinkedinMessageResponse generateEnterpriseLinkedinMessage(EnterpriseLinkedinMessageRequest req){
        String url=baseUrl+"/outreach/generate"; // build URL

        // prepare headers
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); headers.set("X-API-Key",apiKey);

        HttpEntity<EnterpriseLinkedinMessageRequest> entity=new HttpEntity<>(req,headers);

        try{
            // call external API
            ResponseEntity<EnterpriseLinkedinMessageResponse> res=restTemplate.exchange(url,HttpMethod.POST,entity,EnterpriseLinkedinMessageResponse.class);
            EnterpriseLinkedinMessageResponse body=res.getBody();

            // validate response
            if(!res.getStatusCode().is2xxSuccessful()||body==null){
                log.error("LinkedIn message API failed. status={}",res.getStatusCode());
                throw new RestClientException("Failed to generate LinkedIn message");
            }

            // guardrail handling
            if(Boolean.TRUE.equals(body.getGuardrailTriggered()))
                log.warn("Guardrail triggered | orgId={} | reason={}",req.getOrgId(),body.getGuardrailReason());

            return body;

        }catch(RestClientException ex){
            log.error("Error calling LinkedIn message API. msg={}",ex.getMessage(),ex);
            throw ex; // or wrap in ServiceException if needed
        }
    }
}