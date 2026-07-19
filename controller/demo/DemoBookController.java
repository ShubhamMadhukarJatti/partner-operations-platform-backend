package com.sharkdom.controller.demo;

import com.sharkdom.entity.demo.DemoBook;
import com.sharkdom.service.demo.DemoBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/demo-book")
public class DemoBookController {

    private final DemoBookService demoBookService;

    public DemoBookController(DemoBookService demoBookService) {
        this.demoBookService = demoBookService;
    }

    @Operation(summary = "Book new demo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DemoBook.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<DemoBook> saveSalesLead(@RequestBody DemoBook demoBook) {
        return ResponseEntity.ok(demoBookService.create(demoBook));
    }

    @Operation(summary = "Get demos booked between from and to")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DemoBook.class))})})
    @GetMapping("/data")
    public List<DemoBook> getDemos(@Schema(defaultValue = "2024-05-07") @RequestParam(required = false) String from,
                                   @Schema(defaultValue = "2024-05-12") @RequestParam(required = false) String to) {
        return demoBookService.findAllFromTo(from, to);
    }

}
