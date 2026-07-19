package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.DweepHealthResponse;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DweepHealthService {

    private final RestTemplate restTemplate;

    @Value("${dweep.forecast.base-url}")
    private String baseUrl;

    // ================= HEALTH CHECK =================
    public DweepHealthResponse checkHealth(){
        String url=baseUrl+"/health"; // build URL
        log.info("Calling Dweep health API. url={}",url);

        try{
            // call external API
            ResponseEntity<DweepHealthResponse> res=restTemplate.getForEntity(url,DweepHealthResponse.class);
            log.info("Dweep API responded. httpStatus={}",res.getStatusCode());

            // validate response
            if(!res.getStatusCode().is2xxSuccessful()||res.getBody()==null){
                log.error("Dweep API failed. httpStatus={}",res.getStatusCode());
                throw new ServiceException(ErrorMessages.SH160,"Dweep health API failure");
            }

            // success log
            DweepHealthResponse body=res.getBody();
            log.info("Dweep health success. status={}, service={}, version={}",body.getStatus(),body.getService(),body.getVersion());

            return body;

        }catch(RestClientException ex){
            // exception handling
            log.error("Error calling Dweep API. msg={}",ex.getMessage(),ex);
            throw new ServiceException(ErrorMessages.SH160,ex.getMessage());
        }
    }
}