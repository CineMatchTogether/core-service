package com.service.core.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "settings")
public class Setting {

    @Id
    @Column
    private Long id = 1L;

    private String searchToken;

    @Column(columnDefinition = "TEXT")
    private String cookie;
}
