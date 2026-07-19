package com.sharkdom.subscription.model;

import com.sharkdom.subscription.entity.ModuleName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Update Product DTO")
public class UpdateProductDTO {
    private Long id;
    private ModuleName productName;
    private Double priceINR;
    private Double priceUSD;
}
