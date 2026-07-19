package com.sharkdom.entity.trello;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trello_lists")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrelloList {

    @Id
    private String listId;

    private String name;
    private boolean isClosed;
    private Integer position;
    private boolean isSubscribed;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Datasource datasource;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "board_id")
    private TrelloBoard board;

}

