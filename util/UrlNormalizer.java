package com.sharkdom.util;

public class UrlNormalizer {

    public static String normalize(String inputUrl) {
        if (inputUrl == null || inputUrl.isBlank()) return null;

        inputUrl = inputUrl.trim().toLowerCase();

        try {
            // ensure protocol for parsing
            if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                inputUrl = "http://" + inputUrl;
            }

            java.net.URI uri = new java.net.URI(inputUrl);
            String host = uri.getHost();

            if (host == null) {
                throw new RuntimeException("Invalid URL");
            }

            // remove www
            host = host.replaceFirst("^www\\.", "");

            return "https://" + host;

        } catch (Exception e) {
            throw new RuntimeException("Invalid website URL");
        }
    }
}