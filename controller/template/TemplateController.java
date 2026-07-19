package com.sharkdom.controller.template;

import com.sharkdom.entity.template.Template;
import com.sharkdom.model.template.UserTemplatesModel;
import com.sharkdom.service.template.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/templates")

public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @Operation(
            summary = "Get all templates"

    )
    @GetMapping
    public List<Template> getAllTemplates() {
        return templateService.getAllTemplates();
    }

    @Operation(
            summary = "Get a template by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the template"),
            @ApiResponse(responseCode = "404", description = "Template not found with the given ID")
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<Template> getTemplateById(@PathVariable long templateId) {
        return templateService.getTemplateById(templateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a new template"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created the template"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity<Template> createTemplate(@RequestBody Template template) {

        Template createdTemplate = templateService.createTemplate(template);
        return ResponseEntity.ok(createdTemplate);
    }

    @Operation(
            summary = "Update an existing template"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the template"),
            @ApiResponse(responseCode = "404", description = "Template not found with the given ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{templateId}")
    public ResponseEntity<Template> updateTemplate(@PathVariable long templateId, @RequestBody Template updatedTemplate) {
        Template updated = templateService.updateTemplate(templateId, updatedTemplate);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete a template by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the template"),
            @ApiResponse(responseCode = "404", description = "Template not found with the given ID")
    })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Get templates by userId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully templates Fetch by userId"),
            @ApiResponse(responseCode = "404", description = "Template not found with the given ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Template>> templatesList(
            @PathVariable String userId) {

        List<Template> templatesList = templateService.getAllTemplatesAssocitedWithUserId(userId);

        return ResponseEntity.ok(templatesList);
    }

    @Operation(
            summary = "Map templates to user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully mapped the template"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/user")
    public ResponseEntity<String> mapTemplatesToUser(@RequestBody UserTemplatesModel userTemplatesModel) {
        templateService.mapTemplateToUser(userTemplatesModel);
        return ResponseEntity.ok("Success");
    }

    @Operation(
            summary = "Delete templates of user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully deleted the template"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @DeleteMapping("/user")
    public ResponseEntity<String> deleteTemplatesOfUser(@RequestBody UserTemplatesModel userTemplatesModel) {
        templateService.deleteTemplateOfUser(userTemplatesModel);
        return ResponseEntity.ok("Success");
    }

}
