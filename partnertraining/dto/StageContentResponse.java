package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.ContentType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StageContentResponse {

    private Long stageId;
    private String content;
    private ContentType contentType;

    private String thumbnailUrl;
    private String driveLink;
    private String documentLink;
    private String chapterTitle;

    private List<ImageResponse> images;

    @Data
    @Builder
    public static class ImageResponse {
        private Long id;
        private String imageUrl;
        private Integer order;
    }
}