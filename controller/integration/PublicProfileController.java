package com.sharkdom.controller.integration;

import com.sharkdom.entity.integration.EvaluationEntity;
import com.sharkdom.model.integration.CompatibilityRequest;
import com.sharkdom.service.integration.PublicProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

@RequestMapping("/public-profile")
public class PublicProfileController {
    private final PublicProfileService publicProfileService;

    public PublicProfileController(PublicProfileService publicProfileService) {
        this.publicProfileService = publicProfileService;
    }


    @PostMapping()
    public Map<String, String> saveCompatibility(@RequestBody CompatibilityRequest compatibilityRequest) {
        publicProfileService.saveCompatibility(compatibilityRequest);
        return Map.of("message", "request submitted");
    }

    @GetMapping()
    public List<EvaluationEntity> saveCompatibility(@RequestParam String email) {
        return publicProfileService.getCompatibilityScore(email);
    }
}
