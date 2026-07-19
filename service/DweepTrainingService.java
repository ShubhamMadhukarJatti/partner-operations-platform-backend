package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.TrainingExamplesResponse;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DweepTrainingService {

    private final RestTemplate restTemplate;

    @Value("${dweep.forecast.base-url}")
    private String baseUrl;

    @Value("${dweep.forecast.api-key}")
    private String apiKey;

    // ================= TRAINING EXAMPLES =================
    public TrainingExamplesResponse getTrainingExamples(){
        String url=baseUrl+"/training-examples"; // build URL
        log.info("Calling Dweep training API. url={}",url);

        // prepare headers with API key
        HttpHeaders headers=new HttpHeaders(); headers.set("x-api-key",apiKey);
        HttpEntity<Void> entity=new HttpEntity<>(headers);

        try{
            // call external API
            ResponseEntity<TrainingExamplesResponse> res=restTemplate.exchange(url,HttpMethod.GET,entity,TrainingExamplesResponse.class);

            // validate response
            if(!res.getStatusCode().is2xxSuccessful()||res.getBody()==null){
                log.error("Training API failed. status={}",res.getStatusCode());
                throw new ServiceException(ErrorMessages.valueOf("Training examples API failed"));
            }

            // success log
            TrainingExamplesResponse body=res.getBody();
            log.info("Training examples fetched. total={}",body.getCount());

            return body;

        }catch(RestClientException ex){
            // exception handling
            log.error("Error calling training API",ex);
            throw new ServiceException(ErrorMessages.valueOf("Failed to fetch training examples"));
        }
    }
}