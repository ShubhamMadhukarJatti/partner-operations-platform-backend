package com.sharkdom.entity.trello;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trello_boards")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrelloBoard {

    @Id
    private String boardId;

    private String name;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private String url;
    private boolean isClosed;
    private boolean isPinned;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "trelloBoard", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<Membership> memberships;

    public void setMemberships(List<Membership> memberships) {
        memberships.forEach(membership -> membership.setTrelloBoard(this));
        this.memberships = memberships;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "trello_board_members",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<TrelloMember> members = new HashSet<>();

}


