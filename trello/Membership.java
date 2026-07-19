package com.sharkdom.entity.trello;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trello_board_membership")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Membership {


    @Id
    private String id;

    private String idMember;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "board_id")
    @JsonBackReference
    private TrelloBoard trelloBoard;

}
