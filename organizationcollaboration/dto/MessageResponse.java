package com.sharkdom.entity.organizationcollaboration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.organizationcollaboration.ChannelFlag;
import com.sharkdom.entity.organizationcollaboration.OrganizationMessages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Map<ChannelFlag, List<OrganizationMessages>> messageByChannel;
    private long totalMessageCount;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    private List<OrganizationMessages> messages;
    private int totalMemberCount;
    private String spaceName;
    private List<Map<String, Object>> channels;
    private int activeOrganizationCount;
    private List<Map<String, Object>> partners;

    @JsonIgnore
    public List<OrganizationMessages> getMessages() {
        return messageByChannel.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
