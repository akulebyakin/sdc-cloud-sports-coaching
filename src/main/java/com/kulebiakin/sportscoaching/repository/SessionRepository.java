package com.kulebiakin.sportscoaching.repository;

import com.kulebiakin.sportscoaching.entity.Session;
import com.kulebiakin.sportscoaching.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByCoachCoachId(Long coachId);

    List<Session> findByUserUserId(Long userId);

    List<Session> findBySessionStatus(SessionStatus status);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.coach.coachId = :coachId")
    Long countSessionsByCoachId(@Param("coachId") Long coachId);

    @Query("SELECT AVG(s.rating) FROM Session s WHERE s.coach.coachId = :coachId AND s.rating IS NOT NULL")
    Double getAverageRatingByCoachId(@Param("coachId") Long coachId);
}
