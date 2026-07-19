package com.sharkdom.subscription.model;

import com.sharkdom.subscription.entity.ModuleName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ProductResponseDTO {

    private Long id;
    private String productId;
    private ModuleName productName;
    private Double priceINR;
    private Double priceUSD;
}
