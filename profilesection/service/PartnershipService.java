package com.sharkdom.profilesection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.profilesection.client.SharkdomClient;
import com.sharkdom.profilesection.dto.EvaluateApiResponse;
import com.sharkdom.profilesection.dto.EvaluateRequest;
import com.sharkdom.profilesection.dto.PartnershipResponse;
import com.sharkdom.profilesection.dto.PartnershipScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PartnershipService {

    private final SharkdomClient sharkdomClient;

    public PartnershipResponse evaluate(String sentence) throws Exception {

        EvaluateRequest request = new EvaluateRequest();
        request.setSentence(sentence);

        EvaluateApiResponse apiResponse =
                sharkdomClient.evaluate(request);

        List<PartnershipScore> topTwo =
                extractTopTwo(apiResponse.getEvaluation());

        PartnershipResponse response =
                new PartnershipResponse();

        response.setTopPartnerships(topTwo);

        return response;
    }

    private List<PartnershipScore> extractTopTwo(String evaluation) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> list =
                mapper.readValue(evaluation, List.class);

        return list.stream()
                .filter(item -> item.containsKey("percentage"))
                .map(item -> new PartnershipScore(
                        item.get("type").toString(),
                        Integer.valueOf(item.get("percentage").toString())
                ))
                .sorted((a, b) -> b.getPercentage() - a.getPercentage())
                .limit(2)
                .toList();
    }
}
