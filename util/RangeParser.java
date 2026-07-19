package com.sharkdom.util;

public class RangeParser {

    public static class Range {
        private final int start;
        private final Integer end; // null means open-ended (e.g., "1000+")

        public Range(int start, Integer end) {
            this.start = start;
            this.end = end;
        }

        public boolean contains(int value) {
            return value >= start && (end == null || value <= end);
        }

        @Override
        public String toString() {
            return end == null ? start + "+" : start + "-" + end;
        }
    }

    public static Range parse(String input) {
        input = input.trim();
        if (input.endsWith("+")) {
            int start = Integer.parseInt(input.replace("+", "").trim());
            return new Range(start, null); // open-ended
        } else if (input.contains("-")) {
            String[] parts = input.split("-");
            if (parts.length == 2) {
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                return new Range(start, end);
            }
        }
        throw new IllegalArgumentException("Invalid range format: " + input);
    }
}