package com.sharkdom.constants;

public final class PartnerDataFields {
    private PartnerDataFields() {} // Prevent instantiation

    public static final String NAME = "name";
    public static final String COMPANY_NAME = "companyName";
    public static final String CONTACT_EMAIL = "contactEmail";
    public static final String DOMAIN = "domain";
    public static final String DEAL_STAGE = "dealStage";
    public static final String CREATION_DATE = "creationDate";
    public static final String CLOSE_DATE = "closeDate";
    public static final String SUBSCRIBED = "subscribed";
    public static final String TICKET_SIZE = "ticketSize";

    // ---------------- Company Fields ----------------
    public static final String WEBSITE = "website";
    public static final String INDUSTRY = "industry";
    public static final String COMPANY_SIZE = "companySize";
    public static final String COUNTRY = "country";
    public static final String LINKEDIN_URL = "linkedinUrl";
    public static final String ANNUAL_REVENUE = "annualRevenue";
    public static final String DESCRIPTION = "description";
    public static final String COMPANY_PHONE = "companyPhone";
    public static final String CITY = "city";

    // ---------------- Contact Fields ----------------
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String JOB_TITLE = "jobTitle";
    public static final String CONTACT_LINKEDIN_URL = "contactLinkedinUrl";
    public static final String LEAD_STATUS = "leadStatus";
    public static final String CONTACT_PHONE = "contactPhone";
    public static final String LAST_ACTIVITY_DATE = "lastActivityDate";
    public static final String CONTACT_OWNER = "contactOwner";
    public static final String ASSOCIATED_COMPANY_ID = "associatedCompanyId";

    // ---------------- Deal Fields ----------------
    public static final String DEAL_NAME = "dealName";
    public static final String DEAL_OWNER = "dealOwner";
    public static final String AMOUNT_ACV = "amountAcv";
    public static final String HUBSPOT_DEAL_ID = "hubspotDealId";
    public static final String PIPELINE = "pipeline";
    public static final String DEAL_TYPE = "dealType";
    public static final String ASSOCIATED_CONTACT_ID = "associatedContactId";

    // Response field names
    public static final String ORGANIZATION_RECORDS = "organization_records";
    public static final String PARTNER_RECORDS = "partner_records";
    public static final String RECORDS = "records";
    public static final String OVERLAP_COUNT = "overlap_count";
    public static final String MESSAGE = "message";
    public static final String COUNTS = "counts";
    public static final String DATA = "data";
    public static final String MATRIX = "matrix";

    // Matrix section names
    public static final String A_CUSTOMERS = "A_CUSTOMERS";
    public static final String A_PROSPECTS = "A_PROSPECTS";
    public static final String A_OPPORTUNITIES = "A_OPPORTUNITIES";
    public static final String B_CUSTOMERS = "B_CUSTOMERS";
    public static final String B_PROSPECTS = "B_PROSPECTS";
    public static final String B_OPPORTUNITIES = "B_OPPORTUNITIES";
} 