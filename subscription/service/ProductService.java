package com.sharkdom.subscription.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.subscription.entity.ModuleName;
import com.sharkdom.subscription.entity.Product;
import com.sharkdom.subscription.model.CreateProductDTO;
import com.sharkdom.subscription.model.ProductResponseDTO;
import com.sharkdom.subscription.model.UpdateProductDTO;
import com.sharkdom.subscription.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository repository;

    // CREATE
    public ProductResponseDTO createProduct(CreateProductDTO dto) {
        log.info("Creating product with productId: {}", dto.getProductId());

        repository.findByProductId(dto.getProductId())
                .ifPresent(p -> {
                    log.error("Product already exists with productId: {}", dto.getProductId());
                    throw new ServiceException(ErrorMessages.SH85);
                });

        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setPriceINR(dto.getPriceINR());
        product.setPriceUSD(dto.getPriceUSD());
        product.setProductName(dto.getProductName());

        Product saved = repository.save(product);

        log.info("Product created successfully with id: {}", saved.getId());

        return mapToDTO(saved);
    }

    // UPDATE
    public ProductResponseDTO updateProduct(UpdateProductDTO dto) {
        log.info("Updating product id: {}", dto.getId());

        Product product = repository.findById(dto.getId())
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (dto.getProductName() != null) {
            product.setProductName(dto.getProductName());
        }

        Product updated = repository.save(product);

        log.info("Product updated successfully id: {}", updated.getId());

        return mapToDTO(updated);
    }

    // DELETE
    public void deleteProduct(Long id) {
        log.info("Deleting product id: {}", id);

        Product product = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        repository.delete(product);

        log.info("Product deleted successfully id: {}", id);
    }

    // GET ALL
    public List<ProductResponseDTO> getAllProducts() {
        log.info("Fetching all products");

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET BY LIST OF STRING IDS
    public List<ProductResponseDTO> getProductsByProductIds(List<String> productIds) {
        log.info("Fetching products by productIds: {}", productIds);

        List<Product> products = repository.findByProductIdIn(productIds);

        if (products.isEmpty()) {
            log.error("No products found for given productIds");
            throw new ServiceException(ErrorMessages.NOT_FOUND);
        }

        return products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsByModuleNames(List<ModuleName> moduleNames) {

        log.info("Fetching products by moduleNames: {}", moduleNames);

        if (moduleNames == null || moduleNames.isEmpty()) {
            log.error("ModuleNames list is empty");
            throw new ServiceException(ErrorMessages.SH106);
        }

        List<Product> products = repository.findByProductNameIn(moduleNames);

        if (products.isEmpty()) {
            log.error("No products found for given moduleNames: {}", moduleNames);
            throw new ServiceException(ErrorMessages.NOT_FOUND);
        }

        log.info("Found {} products for given moduleNames", products.size());

        return products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // MAPPER
    private ProductResponseDTO mapToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setProductId(product.getProductId());
        dto.setPriceINR(product.getPriceINR());
        dto.setPriceUSD(product.getPriceUSD());
        dto.setProductName(product.getProductName());
        return dto;
    }
}
