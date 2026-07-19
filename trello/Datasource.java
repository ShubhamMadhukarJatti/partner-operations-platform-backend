package com.sharkdom.entity.trello;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trello_list_datasource")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Datasource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonBackReference
    private Long id;

    private boolean isFilter;

}
