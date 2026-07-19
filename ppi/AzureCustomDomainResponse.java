package com.sharkdom.entity.ppi;

public class AzureCustomDomainResponse {

    private String name;
    private String validationToken;

    public AzureCustomDomainResponse(String name, String validationToken) {
        this.name = name;
        this.validationToken = validationToken;
    }

    public String getName() {
        return name;
    }

    public String getValidationToken() {
        return validationToken;
    }
}
