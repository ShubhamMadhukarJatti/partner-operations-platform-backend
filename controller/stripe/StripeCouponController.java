package com.sharkdom.controller.stripe;

import com.sharkdom.model.stripe.StripeCouponDto;
import com.sharkdom.service.stripe.StripeCouponService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/sharkdom-stripe/v1")
@RequiredArgsConstructor
public class StripeCouponController {

    private final StripeCouponService stripeCouponService;

    @Operation(summary = "create coupon")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "create coupon", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCouponDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/coupon")
    public ResponseEntity<StripeCouponDto> createStripeCoupon(@RequestParam(value = "couponName") String couponName, @RequestParam(value = "percentOff") BigDecimal percentOff) throws StripeException {
        return ResponseEntity.status(HttpStatus.CREATED).body(stripeCouponService.createStripeCoupon(couponName, percentOff));
    }

    @Operation(summary = "get all stripe coupons")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get all stripe coupons", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCouponDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/coupons")
    public ResponseEntity<List<StripeCouponDto>> getAllCoupons() throws StripeException {
        return ResponseEntity.status(HttpStatus.OK).body(stripeCouponService.getAllCoupons());
    }

    @Operation(summary = "get coupon by couponId/couponCode")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get coupon by couponId/couponCode", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCouponDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/coupon/{couponCode}")
    public ResponseEntity<StripeCouponDto> getStripeCouponByCouponId(@PathVariable(value = "couponCode") String couponCode) throws StripeException {
        return ResponseEntity.status(HttpStatus.OK).body(stripeCouponService.getStripeCouponByCouponId(couponCode));
    }

    @Operation(summary = "delete coupon by couponId/couponCode")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "delete coupon by couponId/couponCode", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCouponDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @DeleteMapping("/coupon/{couponCode}")
    public ResponseEntity<StripeCouponDto> deleteStripeCouponByCouponId(@PathVariable(value = "couponCode") String couponCode) throws StripeException {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(stripeCouponService.deleteStripeCouponByCouponId(couponCode));
    }

}
