package com.service.core.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user_movie_history")
@AllArgsConstructor
@NoArgsConstructor
public class UserMovieHistory {

    @EmbeddedId
    private UserMovieHistoryPK userMovieHistoryPK;

}

