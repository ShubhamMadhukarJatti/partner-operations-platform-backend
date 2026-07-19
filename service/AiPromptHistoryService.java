package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.entity.AiPromptHistory;
import com.sharkdom.agenticai.model.*;
import com.sharkdom.agenticai.repository.AiPromptHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiPromptHistoryService {

    private final AiPromptHistoryRepository repository;

    // ================= SAVE =================
    public AiPromptHistoryResponse savePromptHistory(AiPromptHistoryRequest req){
        // map request -> entity
        AiPromptHistory h=new AiPromptHistory();
        h.setOrgId(req.getOrgId()); h.setUserId(req.getUserId());
        h.setPrompt(req.getPrompt()); h.setOutputResultId(req.getOutputResultId());

        // save to DB
        h=repository.save(h);

        // map entity -> response
        AiPromptHistoryResponse res=new AiPromptHistoryResponse();
        res.setId(h.getId()); res.setOrgId(h.getOrgId()); res.setUserId(h.getUserId());
        res.setPrompt(h.getPrompt()); res.setOutputResultId(h.getOutputResultId());

        return res;
    }

    // ================= GET BY ORG =================
    public List<AiPromptHistoryResponse> getPromptHistoryByOrgId(Long orgId){
        return repository.findByOrgId(orgId).stream().map(h->{
            AiPromptHistoryResponse res=new AiPromptHistoryResponse();
            res.setId(h.getId()); res.setOrgId(h.getOrgId()); res.setUserId(h.getUserId());
            res.setPrompt(h.getPrompt()); res.setOutputResultId(h.getOutputResultId());
            return res;
        }).collect(Collectors.toList());
    }
}