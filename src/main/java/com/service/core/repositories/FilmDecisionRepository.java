package com.service.core.repositories;

import com.service.core.models.entities.FilmDecision;
import com.service.core.models.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FilmDecisionRepository extends JpaRepository<FilmDecision, Long> {

    @Query("SELECT fs FROM FilmDecision fs WHERE fs.userSession.userSessionPK.session.id = :sessionId")
    List<FilmDecision> findBySessionId(String sessionId);

    Boolean existsByUserSessionAndMovieId(UserSession userSession, Long movieId);
}
