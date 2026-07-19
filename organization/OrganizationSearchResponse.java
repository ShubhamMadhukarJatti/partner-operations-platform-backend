package com.sharkdom.model.organization;

public interface OrganizationSearchResponse {


    long getId();

    String getCode();

    String getName();

    String getAbout();

    String getBriefDescription();

    String getSector();

    String getStage();

    String getCity();

    String getState();

    boolean getVerified();

    int getInceptionYear();

    String getTargetMarket();
    Double getRating();
    String getLogoUrl();
    String getSectorType();
    String getCompanyType();
    Long getAcknowledgmentTime();
    Long getActivePartnerships();
    Long getPipelinePartnerships();
}
