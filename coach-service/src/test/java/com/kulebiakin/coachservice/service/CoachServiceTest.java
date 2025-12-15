package com.kulebiakin.coachservice.service;

import com.kulebiakin.coachservice.entity.Coach;
import com.kulebiakin.coachservice.entity.CoachStatus;
import com.kulebiakin.coachservice.repository.CoachRepository;
import com.kulebiakin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoachServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private CoachService coachService;

    private Coach testCoach;

    @BeforeEach
    void setUp() {
        testCoach = Coach.builder()
            .coachId(1L)
            .firstName("John")
            .lastName("Doe")
            .rating(BigDecimal.valueOf(7.5))
            .strikeCount(0)
            .coachStatus(CoachStatus.ACTIVE)
            .build();
    }

    @Test
    void getAllCoaches_returnsListOfCoaches() {
        List<Coach> coaches = Arrays.asList(testCoach, Coach.builder().coachId(2L).build());
        when(coachRepository.findAll()).thenReturn(coaches);

        List<Coach> result = coachService.getAllCoaches();

        assertThat(result).hasSize(2);
        verify(coachRepository).findAll();
    }

    @Test
    void getCoachById_existingId_returnsCoach() {
        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));

        Coach result = coachService.getCoachById(1L);

        assertThat(result).isEqualTo(testCoach);
        verify(coachRepository).findById(1L);
    }

    @Test
    void getCoachById_nonExistingId_throwsException() {
        when(coachRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachService.getCoachById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCoachesByStatus_returnsFilteredCoaches() {
        List<Coach> activeCoaches = Collections.singletonList(testCoach);
        when(coachRepository.findByCoachStatus(CoachStatus.ACTIVE)).thenReturn(activeCoaches);

        List<Coach> result = coachService.getCoachesByStatus(CoachStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoachStatus()).isEqualTo(CoachStatus.ACTIVE);
    }

    @Test
    void createCoach_savesAndReturnsCoach() {
        when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

        Coach result = coachService.createCoach(testCoach);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(coachRepository).save(testCoach);
    }

    @Test
    void updateCoach_existingId_updatesAndReturnsCoach() {
        Coach updatedDetails = Coach.builder()
            .firstName("Jane")
            .lastName("Smith")
            .build();

        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
        when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

        Coach result = coachService.updateCoach(1L, updatedDetails);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        verify(coachRepository).save(testCoach);
    }

    @Test
    void updateCoach_nonExistingId_throwsException() {
        when(coachRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachService.updateCoach(99L, testCoach))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCoachRating_normalRating_updatesRating() {
        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
        when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

        Coach result = coachService.updateCoachRating(1L, BigDecimal.valueOf(8.0), 10L);

        assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(8.0));
        assertThat(result.getStrikeCount()).isZero();
        assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.ACTIVE);
    }

    @Test
    void updateCoachRating_lowRating_incrementsStrike() {
        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
        when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

        Coach result = coachService.updateCoachRating(1L, BigDecimal.valueOf(1.5), 10L);

        assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(1.5));
        assertThat(result.getStrikeCount()).isEqualTo(1);
        assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.ACTIVE);
    }

    @Test
    void updateCoachRating_fifthStrike_deactivatesCoach() {
        testCoach.setStrikeCount(4);
        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
        when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

        Coach result = coachService.updateCoachRating(1L, BigDecimal.valueOf(1.0), 10L);

        assertThat(result.getStrikeCount()).isEqualTo(5);
        assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.DEACTIVATED);
    }

    @Test
    void updateCoachRating_ratingExactlyTwo_noStrike() {
        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
        when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

        Coach result = coachService.updateCoachRating(1L, BigDecimal.valueOf(2.0), 10L);

        assertThat(result.getStrikeCount()).isZero();
    }

    @Test
    void updateCoachStatus_changesStatus() {
        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
        when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

        Coach result = coachService.updateCoachStatus(1L, CoachStatus.DEACTIVATED);

        assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.DEACTIVATED);
    }

    @Test
    void deleteCoach_existingId_deletesCoach() {
        when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));

        coachService.deleteCoach(1L);

        verify(coachRepository).delete(testCoach);
    }

    @Test
    void deleteCoach_nonExistingId_throwsException() {
        when(coachRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachService.deleteCoach(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
