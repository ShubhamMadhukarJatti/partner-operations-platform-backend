package com.sharkdom.util;

import com.sharkdom.model.ai.PercentageCategory;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class CategoryComparator {
    public static double calculatePercentageMatch(List<PercentageCategory> list1, List<PercentageCategory> list2) {
        if (list1 == null || list2 == null) return 0.0;

//        Map<String, Double> map1 = list1.stream()
//                .collect(Collectors.toMap(PercentageCategory::getKey, PercentageCategory::getPercentage));
        Map<String, Double> map1 = list1.stream()
                .collect(Collectors.toMap(
                        PercentageCategory::getKey,
                        PercentageCategory::getPercentage,
                        (existing, replacement) -> existing
                ));

//        Map<String, Double> map2 = list2.stream()
//                .collect(Collectors.toMap(PercentageCategory::getKey, PercentageCategory::getPercentage));
        Map<String, Double> map2 = list2.stream()
                .collect(Collectors.toMap(
                        PercentageCategory::getKey,
                        PercentageCategory::getPercentage,
                        (existing, replacement) -> existing
                ));
        List<Double> averageValues = new ArrayList<>();

        for (String key : map1.keySet()) {
            if (map2.containsKey(key)) {
                double avg = (map1.get(key) + map2.get(key)) / 2.0;
                averageValues.add(avg);
            }
        }

        return averageValues.isEmpty() ? 0.0 :
                averageValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public static String getLargestMatchingCompanySize(List<PercentageCategory> size1, List<PercentageCategory> size2) {
        if (size1 == null || size2 == null) return null;

        Set<String> keys2 = size2.stream()
                .map(PercentageCategory::getKey)
                .collect(Collectors.toSet());

        return size1.stream()
                .filter(p -> keys2.contains(p.getKey()))
                .map(p -> CompanySizeRank.fromLabel(p.getKey()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingInt(CompanySizeRank::getRank))
                .map(CompanySizeRank::getLabel)
                .orElse(null);
    }

    // Company size ranking
    public enum CompanySizeRank {
        LOW("Small enterprises", 1),
        HIGH("Medium enterprises", 2),
        HIGHEST("Large enterprises", 3);

        private final String label;
        private final int rank;

        CompanySizeRank(String label, int rank) {
            this.label = label;
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }

        public String getLabel() {
            return label;
        }

        public static Optional<CompanySizeRank> fromLabel(String label) {
            return Arrays.stream(values())
                    .filter(e -> e.label.equalsIgnoreCase(label))
                    .findFirst();
        }
    }

    public static List<PercentageCategory> calculateBreakdownFromLists(
            List<PercentageCategory> list1,
            List<PercentageCategory> list2) {

        if (list1 == null || list2 == null) return Collections.emptyList();

//        Map<String, Double> map1 = list1.stream()
//                .collect(Collectors.toMap(PercentageCategory::getKey, PercentageCategory::getPercentage));
//
//        Map<String, Double> map2 = list2.stream()
//                .collect(Collectors.toMap(PercentageCategory::getKey, PercentageCategory::getPercentage));
        Map<String, Double> map1 = list1.stream()
                .collect(Collectors.toMap(
                        PercentageCategory::getKey,
                        PercentageCategory::getPercentage,
                        (existing, replacement) -> existing // keep first occurrence if duplicate
                ));

        Map<String, Double> map2 = list2.stream()
                .collect(Collectors.toMap(
                        PercentageCategory::getKey,
                        PercentageCategory::getPercentage,
                        (existing, replacement) -> existing // keep first occurrence if duplicate
                ));


        List<PercentageCategory> breakdownList = new ArrayList<>();

        for (String key : map1.keySet()) {
            if (map2.containsKey(key)) {
                double val = Math.min(map1.get(key), map2.get(key));
                breakdownList.add(new PercentageCategory(key, val));
            }
        }

        return breakdownList;
    }

}
