package com.sharkdom.offlinePartner.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag_options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    private TableColumn column;
}
