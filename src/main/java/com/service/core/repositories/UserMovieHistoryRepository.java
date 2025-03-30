package com.service.core.repositories;

import com.service.core.models.entities.UserMovieHistory;
import com.service.core.models.entities.UserMovieHistoryPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMovieHistoryRepository extends JpaRepository<UserMovieHistory, UserMovieHistoryPK> {

}
