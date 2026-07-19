package com.sharkdom.entity.trello;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trello_members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrelloMember {

    @Id
    private String memberId;

    private String username;
    private String fullName;
    private String email;
    private String url;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
