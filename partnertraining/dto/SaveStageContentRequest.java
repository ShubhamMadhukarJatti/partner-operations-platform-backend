package com.sharkdom.partnertraining.dto;

import com.sharkdom.partnertraining.enums.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SaveStageContentRequest {

    @Schema(example = "Lorem ipsum dolor sit amet...")
    private String content;

    @Schema(example = "VIDEO")
    private ContentType contentType;

    @Schema(example = "https://cdn.example.com/thumb.jpg")
    private String thumbnailUrl;

    @Schema(example = "https://drive.google.com/file/xyz")
    private String driveLink;

    @Schema(example = "https://cdn.example.com/file.pdf")
    private String documentLink;

    @Schema(example = "title of the chapter")
    private String chapterTitle;

    @Schema(
            example = "[\"https://cdn/img1.png\", \"https://cdn/img2.png\"]"
    )
    private List<String> imageUrls;


}