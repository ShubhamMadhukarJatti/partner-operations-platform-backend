package com.sharkdom.entity.typeform;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TYPEFORM_EVENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeformEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String eventId;

    private String eventDetail;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String formResponse;

}
