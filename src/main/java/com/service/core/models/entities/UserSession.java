package com.service.core.models.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user_session")
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {

    @EmbeddedId
    private UserSessionPK userSessionPK;
}
