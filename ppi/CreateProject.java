package com.sharkdom.entity.ppi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "create_project_ppi")
public class CreateProject extends BaseEntity {

    @JsonProperty("scriptId")
    private String scriptId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("webBookUrl")
    private String webBookUrl;
@Lob
    @JsonProperty("script")
    private String script;

    @JsonProperty("createTime")
    private String createTime;

    @JsonProperty("updateTime")
    private String updateTime;

    private Long orgId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "domain", column = @Column(name = "creator_domain")),
            @AttributeOverride(name = "email", column = @Column(name = "creator_email")),
            @AttributeOverride(name = "name", column = @Column(name = "creator_name")),
            @AttributeOverride(name = "photoUrl", column = @Column(name = "creator_photo_url"))
    })
    @JsonProperty("creator")
    private UserInfo creator;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "domain", column = @Column(name = "last_modify_domain")),
            @AttributeOverride(name = "email", column = @Column(name = "last_modify_email")),
            @AttributeOverride(name = "name", column = @Column(name = "last_modify_name")),
            @AttributeOverride(name = "photoUrl", column = @Column(name = "last_modify_photo_url"))
    })
    @JsonProperty("lastModifyUser")
    private UserInfo lastModifyUser;
}

