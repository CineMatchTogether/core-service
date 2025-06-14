package com.service.core.repositories;

import com.service.core.models.entities.UserSession;
import com.service.core.models.entities.UserSessionPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, UserSessionPK> {

    @Query("SELECT us FROM UserSession us WHERE us.userSessionPK.session.id = :sessionId")
    List<UserSession> findBySessionId(String sessionId);
}
