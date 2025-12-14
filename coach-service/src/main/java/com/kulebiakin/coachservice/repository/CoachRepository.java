package com.kulebiakin.coachservice.repository;

import com.kulebiakin.coachservice.entity.Coach;
import com.kulebiakin.coachservice.entity.CoachStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {

    List<Coach> findByCoachStatus(CoachStatus status);

    List<Coach> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}
