package com.sharkdom.model.stripe;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class StripeAccountResponse {

    public String id;
    public String object;
    public BusinessProfile business_profile;
    public String business_type;
    public Capabilities capabilities;
    public boolean charges_enabled;
    public Controller controller;
    public String country;
    public long created;
    public String default_currency;
    public boolean details_submitted;
    public String email;
    public ExternalAccounts external_accounts;
    public FutureRequirements future_requirements;
    public LoginLinks login_links;
    public Map<String, Object> metadata;
    public boolean payouts_enabled;
    public Requirements requirements;
    public Settings settings;
    public TosAcceptance tos_acceptance;
    public String type;

    public static class BusinessProfile {
        public Object annual_revenue;
        public Object estimated_worker_count;
        public String mcc;
        public Object minority_owned_business_designation;
        public String name;
        public String product_description;
        public Object support_address;
        public String support_email;
        public String support_phone;
        public String support_url;
        public String url;
    }

    public static class Capabilities {
        public String transfers;
    }

    public static class Controller {
        public Fees fees;
        public boolean is_controller;
        public Losses losses;
        public String requirement_collection;
        public StripeDashboard stripe_dashboard;
        public String type;

        public static class Fees {
            public String payer;
        }

        public static class Losses {
            public String payments;
        }

        public static class StripeDashboard {
            public String type;
        }
    }

    public static class ExternalAccounts {
        public String object;
        public List<Object> data;
        public boolean has_more;

        @JsonProperty("total_count")
        public int totalCount;

        public String url;
    }

    public static class FutureRequirements {
        public List<Object> alternatives;
        public Object current_deadline;

        @JsonProperty("currently_due")
        public List<String> currentlyDue;

        @JsonProperty("disabled_reason")
        public String disabledReason;

        public List<Object> errors;

        @JsonProperty("eventually_due")
        public List<String> eventuallyDue;

        @JsonProperty("past_due")
        public List<String> pastDue;

        @JsonProperty("pending_verification")
        public List<Object> pendingVerification;
    }

    public static class LoginLinks {
        public String object;
        public List<Object> data;
        public boolean has_more;

        @JsonProperty("total_count")
        public int totalCount;

        public String url;
    }

    public static class Requirements {
        public List<Object> alternatives;
        public Object current_deadline;

        @JsonProperty("currently_due")
        public List<String> currentlyDue;

        @JsonProperty("disabled_reason")
        public String disabledReason;

        public List<Object> errors;

        @JsonProperty("eventually_due")
        public List<String> eventuallyDue;

        @JsonProperty("past_due")
        public List<String> pastDue;

        @JsonProperty("pending_verification")
        public List<Object> pendingVerification;
    }

    public static class Settings {
        @JsonProperty("bacs_debit_payments")
        public BacsDebitPayments bacsDebitPayments;

        public Branding branding;

        @JsonProperty("card_issuing")
        public CardIssuing cardIssuing;

        @JsonProperty("card_payments")
        public CardPayments cardPayments;

        public Dashboard dashboard;
        public Invoices invoices;
        public Payments payments;
        public Payouts payouts;

        @JsonProperty("sepa_debit_payments")
        public Object sepaDebitPayments;

        public static class BacsDebitPayments {
            public String display_name;
            public String service_user_number;
        }

        public static class Branding {
            public String icon;
            public String logo;
            public String primary_color;
            public String secondary_color;
        }

        public static class CardIssuing {
            @JsonProperty("tos_acceptance")
            public TosAcceptance tosAcceptance;
        }

        public static class CardPayments {
            @JsonProperty("decline_on")
            public DeclineOn declineOn;

            public String statement_descriptor_prefix;
            public String statement_descriptor_prefix_kana;
            public String statement_descriptor_prefix_kanji;
        }

        public static class DeclineOn {
            public boolean avs_failure;
            public boolean cvc_failure;
        }

        public static class Dashboard {
            public String display_name;
            public String timezone;
        }

        public static class Invoices {
            public Object default_account_tax_ids;

            @JsonProperty("hosted_payment_method_save")
            public String hostedPaymentMethodSave;
        }

        public static class Payments {
            public String statement_descriptor;
            public String statement_descriptor_kana;
            public String statement_descriptor_kanji;
        }

        public static class Payouts {
            public boolean debit_negative_balances;
            public Schedule schedule;
            public String statement_descriptor;

            public static class Schedule {
                public int delay_days;
                public String interval;
            }
        }
    }

    public static class TosAcceptance {
        public Long date;
        public String ip;

        @JsonProperty("user_agent")
        public String userAgent;
    }
}

