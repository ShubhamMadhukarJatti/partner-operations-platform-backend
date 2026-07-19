package com.sharkdom.subscription.controller;

import com.sharkdom.subscription.model.CreateProductDTO;
import com.sharkdom.subscription.model.ModuleNameRequestDTO;
import com.sharkdom.subscription.model.ProductResponseDTO;
import com.sharkdom.subscription.model.UpdateProductDTO;
import com.sharkdom.subscription.service.ProductService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService service;

    @Operation(summary = "Create product using ModuleName enum")
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody CreateProductDTO dto) {

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Product created",
                        service.createProduct(dto))
        );
    }

    @Operation(summary = "Update product")
    @PutMapping
    public ResponseEntity<?> updateProduct(@RequestBody UpdateProductDTO dto) {

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Updated",
                        service.updateProduct(dto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {

        service.deleteProduct(id);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Deleted", null)
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts() {

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Fetched",
                        service.getAllProducts())
        );
    }

    @Operation(summary = "Get products by ModuleName list")
    @PostMapping("/by-modules")
    public ResponseEntity<?> getProductsByModules(@RequestBody ModuleNameRequestDTO dto) {

        log.info("API request: Fetch products by module names");

        List<ProductResponseDTO> response =
                service.getProductsByModuleNames(dto.getModuleNames());

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Fetched successfully", response)
        );
    }


}