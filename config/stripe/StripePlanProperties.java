//package com.sharkdom.config.stripe;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
//@Setter
//@Getter
//@Configuration
//@ConfigurationProperties(prefix = "stripe.plan")
//public class StripePlanProperties {
//    // Getters and Setters
//    private Plan standard;
//    private Plan premium;
//    private Plan enterprise;
//
//    @Setter
//    @Getter
//    public static class Plan {
//        // Getters and Setters
//        private Frequency monthly;
//        private Frequency yearly;
//
//    }
//
//    @Setter
//    @Getter
//    public static class Frequency {
//        // Getters and Setters
//        private String inr;
//        private String us;
//        private String one;
//        private String two;
//        private String three;
//        private String four;
//
//    }
//}
