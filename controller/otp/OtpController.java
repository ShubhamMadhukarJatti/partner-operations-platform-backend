package com.sharkdom.controller.otp;

import com.sharkdom.model.otp.OneTimePassword;
import com.sharkdom.service.otp.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @Operation(summary = "generate otp ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "generate otp", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = OneTimePassword.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateOTP(@RequestParam String email) throws Exception {
        Map<String, String> otpResponse = otpService.generateOTP(email, "email_verify_otp");
        return ResponseEntity.ok(otpResponse);
    }

    @Operation(summary = "validate otp ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "validate otp", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateOTP(@RequestParam String email, @RequestParam String selectedOTP) {
        return ResponseEntity.ok(otpService.validateOTP(email, selectedOTP));
    }

}
