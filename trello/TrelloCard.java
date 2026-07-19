package com.sharkdom.entity.trello;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trello_cards")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrelloCard {

    @Id
    private String cardId;

    private String name;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private String url;
    private LocalDateTime dueDate;
    private boolean isClosed;
    private Integer position;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "list_id")
    private TrelloList list;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "board_id")
    private TrelloBoard board;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "trello_card_members",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<TrelloMember> members = new HashSet<>();

}
