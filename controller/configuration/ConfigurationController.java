package com.sharkdom.controller.configuration;

import com.sharkdom.entity.configuration.Configuration;
import com.sharkdom.service.configuration.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

@RequestMapping("/configuration")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @Operation(summary = "Create/update a configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Configuration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<Configuration> saveConfiguration(@RequestBody Configuration configuration) {
        return ResponseEntity.ok(configurationService.saveConfiguration(configuration));
    }

    @Operation(summary = "Get list of active configurations by their type and one or more keys")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Configuration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allActiveByTypeAndKeys")
    public ResponseEntity<List<Configuration>> findAllActiveByTypeAndKeys(@RequestParam(name = "type") String type,
                                                                          @RequestParam(name = "webApplicable") boolean webApplicable,
                                                                          @RequestParam(name = "appApplicable") boolean appApplicable,
                                                                          @RequestParam(name = "keys") List<String> keys) {
        return ResponseEntity.ok(configurationService.findAllActiveByTypeAndKeyIn(type, webApplicable, appApplicable, keys));
    }

    @Operation(summary = "Get list of active configurations by their type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Configuration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allActiveByType")
    public ResponseEntity<List<Configuration>> findAllActiveByType(@RequestParam(name = "type") String type) {
        return ResponseEntity.ok(configurationService.findAllActiveByType(type));
    }

    @Operation(summary = "Get list of all configurations by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Configuration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allByType")
    public ResponseEntity<List<Configuration>> findAllByType(@RequestParam(name = "type") String type) {
        return ResponseEntity.ok(configurationService.findAllByType(type));
    }

    @Operation(summary = "Get list of all configurations by type comma separated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Configuration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allByTypes")
    public ResponseEntity<Map<String, List<Configuration>>> findAllByTypes(@RequestParam(name = "type") String type) {
        return ResponseEntity.ok(configurationService.findAllByTypes(type));
    }

    @Operation(summary = "Get list of all configurations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Configuration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/all")
    public ResponseEntity<Page<Configuration>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(configurationService.findAll(page, size));
    }

    @Operation(summary = "Search for values by config_type partial value, case insensitive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Configuration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/searchByPartialValue")
    public ResponseEntity<Page<Configuration>> searchByPartialName(@RequestParam(name = "configType") String configType,
                                                                   @RequestParam(name = "partialKey") String partialValue,
                                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                                   @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(configurationService.searchByPartialValue(configType, partialValue, page, size));
    }

}
