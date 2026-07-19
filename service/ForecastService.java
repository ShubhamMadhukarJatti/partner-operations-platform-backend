package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.*;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastService {

    private final RestTemplate restTemplate;

    @Value("${dweep.forecast.base-url}")
    private String baseUrl;

    @Value("${dweep.forecast.api-key}")
    private String apiKey;

    // ================= SINGLE FORECAST =================
    public ForecastResponse generateForecast(ForecastRequest req){
        if(req.getORGid()==null){log.error("Forecast failed. orgId null"); throw new ServiceException(ErrorMessages.SH106);}

        String url=baseUrl+"/forecast"; log.info("Calling Forecast API | orgId={} | url={}",req.getORGid(),url);

        HttpHeaders headers=new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON); headers.set("X-API-Key",apiKey);
        HttpEntity<ForecastRequest> entity=new HttpEntity<>(req,headers);

        try{
            ResponseEntity<ForecastResponse> res=restTemplate.exchange(url,HttpMethod.POST,entity,ForecastResponse.class);
            log.info("Forecast API response | orgId={} | status={}",req.getORGid(),res.getStatusCode());

            if(!res.getStatusCode().is2xxSuccessful()||res.getBody()==null){
                log.error("Forecast API failed | orgId={} | status={}",req.getORGid(),res.getStatusCode());
                throw new ServiceException(ErrorMessages.SH160,"Forecast API failure");
            }

            log.info("Forecast success | orgId={}",req.getORGid());
            return res.getBody();

        }catch(RestClientException ex){
            log.error("Forecast API exception | orgId={} | msg={}",req.getORGid(),ex.getMessage(),ex);
            throw new ServiceException(ErrorMessages.SH160,ex.getMessage());
        }
    }

    // ================= BATCH FORECAST =================
    public List<ForecastBatchResponse> generateBatchForecast(List<ForecastBatchRequest> req){
        if(req==null||req.isEmpty()){log.error("Batch forecast failed. empty request"); throw new ServiceException(ErrorMessages.SH106);}

        String url=baseUrl+"/forecast/batch"; log.info("Calling Batch Forecast API | url={} | size={}",url,req.size());

        HttpHeaders headers=new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON); headers.set("x-api-key",apiKey);
        HttpEntity<List<ForecastBatchRequest>> entity=new HttpEntity<>(req,headers);

        try{
            ResponseEntity<List<ForecastBatchResponse>> res=restTemplate.exchange(url,HttpMethod.POST,entity,new ParameterizedTypeReference<List<ForecastBatchResponse>>(){});
            log.info("Batch Forecast API response | status={}",res.getStatusCode());

            if(!res.getStatusCode().is2xxSuccessful()||res.getBody()==null){
                log.error("Batch Forecast API failed | status={}",res.getStatusCode());
                throw new ServiceException(ErrorMessages.SH160,"Batch forecast API failure");
            }

            log.info("Batch Forecast success | results={}",res.getBody().size());
            return res.getBody();

        }catch(RestClientException ex){
            log.error("Batch Forecast API exception | msg={}",ex.getMessage(),ex);
            throw new ServiceException(ErrorMessages.SH160,ex.getMessage());
        }
    }
}