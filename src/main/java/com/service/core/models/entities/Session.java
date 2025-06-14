package com.service.core.models.entities;

import com.service.core.models.entities.enums.ESessionStatus;
import com.service.core.services.CustomIdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sessions")
public class Session {

    @Id
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    private ESessionStatus sessionStatus;

    @Lob
    @Column(name = "movies_ids")
    private Serializable moviesIds;

    public List<Long> getMoviesIds() {
        return (List<Long>) moviesIds;
    }

    public void setMoviesIds(List<Long> moviesIds) {
        this.moviesIds = (Serializable) moviesIds;
    }

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) {
            id = CustomIdGenerator.generate();
        }
    }

}
