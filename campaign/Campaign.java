package com.sharkdom.model.campaign;

import com.sharkdom.constants.campaign.CampaignType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Campaign {

    public String status;
    public CampaignType campaignType;
    public String since;
    public String title;
    public String subtitle;
    public String purposeText;
    public String frequency;
    public TemplateWorkFlow templateWorkFlow;

    @Override
    public String toString() {
        return "{" +
                "\"status\":\"" + status + "\"," +
                "\"campaignType\":\"" + campaignType + "\"," +
                "\"since\":\"" + since + "\"," +
                "\"title\":\"" + title + "\"," +
                "\"subtitle\":\"" + subtitle + "\"," +
                "\"purposeText\":\"" + purposeText + "\"," +
                "\"frequency\":\"" + frequency + "\"," +
                "\"templateWorkFlow\":" + templateWorkFlow +
                "}";
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TemplateWorkFlow {
        public List<Node> nodes;
        public List<Edge> edges;
        public List<ConditionInternal> conditions;

        @Override
        public String toString() {
            return "{" +
                    "\"nodes\":" + nodes + "," +
                    "\"edges\":" + edges + "," +
                    "\"conditions\":" + conditions +
                    "}";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Node {
        public String id;
        public String type;
        public NodeData data;
        public Position position;

        @Override
        public String toString() {
            return "{" +
                    "\"id\":\"" + id + "\"," +
                    "\"type\":\"" + type + "\"," +
                    "\"data\":" + data + "," +
                    "\"position\":" + position +
                    "}";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class NodeData {
        public String title;
        public String icon;
        public String subtitle;
        public String borderColor;
        public Boolean allowAddingCondition;

        @Override
        public String toString() {
            return "{" +
                    "\"title\":\"" + title + "\"," +
                    "\"icon\":\"" + icon + "\"," +
                    "\"subtitle\":\"" + subtitle + "\"," +
                    "\"borderColor\":\"" + borderColor + "\"," +
                    "\"allowAddingCondition\":" + allowAddingCondition +
                    "}";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Position {
        public int x;
        public int y;

        @Override
        public String toString() {
            return "{" +
                    "\"x\":" + x + "," +
                    "\"y\":" + y +
                    "}";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Edge {
        public String id;
        public String source;
        public String target;
        public EdgeData data;
        public String type;
        public Boolean animated;

        @Override
        public String toString() {
            return "{" +
                    "\"id\":\"" + id + "\"," +
                    "\"source\":\"" + source + "\"," +
                    "\"target\":\"" + target + "\"," +
                    "\"data\":" + data + "," +
                    "\"type\":\"" + type + "\"," +
                    "\"animated\":" + animated +
                    "}";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EdgeData {
        public String label;

        @Override
        public String toString() {
            return "{" +
                    "\"label\":\"" + label + "\"" +
                    "}";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ConditionInternal {
        public String conditionLabel;
        public Integer templateId;
        public Integer delay;
        public Integer activefor;
        public List<Integer> nodeIdsUnderCondition;

        @Override
        public String toString() {
            return "{" +
                    "\"conditionLabel\":\"" + conditionLabel + "\"," +
                    "\"templateId\":" + templateId + "," +
                    "\"delay\":" + delay + "," +
                    "\"activefor\":" + activefor + "," +
                    "\"nodeIdsUnderCondition\":" + nodeIdsUnderCondition +
                    "}";
        }
    }
}
