package com.kulebiakin.sportscoaching.service;

import com.kulebiakin.sportscoaching.entity.Coach;
import com.kulebiakin.sportscoaching.entity.CoachStatus;
import com.kulebiakin.sportscoaching.repository.CoachRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
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
                .rating(BigDecimal.valueOf(8.5))
                .strikeCount(0)
                .coachStatus(CoachStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("getAllCoaches")
    class GetAllCoaches {

        @Test
        @DisplayName("should return all coaches")
        void shouldReturnAllCoaches() {
            Coach coach2 = Coach.builder()
                    .coachId(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            when(coachRepository.findAll()).thenReturn(Arrays.asList(testCoach, coach2));

            List<Coach> result = coachService.getAllCoaches();

            assertThat(result).hasSize(2);
            verify(coachRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no coaches exist")
        void shouldReturnEmptyListWhenNoCoaches() {
            when(coachRepository.findAll()).thenReturn(List.of());

            List<Coach> result = coachService.getAllCoaches();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCoachById")
    class GetCoachById {

        @Test
        @DisplayName("should return coach when found")
        void shouldReturnCoachWhenFound() {
            when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));

            Coach result = coachService.getCoachById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("should throw exception when coach not found")
        void shouldThrowExceptionWhenNotFound() {
            when(coachRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> coachService.getCoachById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Coach not found with id: 99");
        }
    }

    @Nested
    @DisplayName("createCoach")
    class CreateCoach {

        @Test
        @DisplayName("should create coach successfully")
        void shouldCreateCoach() {
            when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

            Coach result = coachService.createCoach(testCoach);

            assertThat(result).isNotNull();
            assertThat(result.getCoachId()).isEqualTo(1L);
            verify(coachRepository).save(testCoach);
        }
    }

    @Nested
    @DisplayName("updateCoachRating")
    class UpdateCoachRating {

        @Test
        @DisplayName("should update rating without strike when rating >= 2")
        void shouldUpdateRatingWithoutStrike() {
            when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
            when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

            Coach result = coachService.updateCoachRating(1L, BigDecimal.valueOf(7.5), 10L);

            assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(7.5));
            assertThat(result.getStrikeCount()).isEqualTo(0);
            assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.ACTIVE);
        }

        @Test
        @DisplayName("should add strike when rating < 2")
        void shouldAddStrikeWhenLowRating() {
            testCoach.setStrikeCount(0);
            when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
            when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

            Coach result = coachService.updateCoachRating(1L, BigDecimal.valueOf(1.5), 5L);

            assertThat(result.getStrikeCount()).isEqualTo(1);
            assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.ACTIVE);
        }

        @Test
        @DisplayName("should deactivate coach when reaching 5 strikes")
        void shouldDeactivateCoachOnFifthStrike() {
            testCoach.setStrikeCount(4);
            when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
            when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

            Coach result = coachService.updateCoachRating(1L, BigDecimal.valueOf(1.0), 10L);

            assertThat(result.getStrikeCount()).isEqualTo(5);
            assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.DEACTIVATED);
        }
    }

    @Nested
    @DisplayName("updateCoachStatus")
    class UpdateCoachStatus {

        @Test
        @DisplayName("should update status to deactivated")
        void shouldUpdateStatusToDeactivated() {
            when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
            when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

            Coach result = coachService.updateCoachStatus(1L, CoachStatus.DEACTIVATED);

            assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.DEACTIVATED);
        }

        @Test
        @DisplayName("should update status to active")
        void shouldUpdateStatusToActive() {
            testCoach.setCoachStatus(CoachStatus.DEACTIVATED);
            when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
            when(coachRepository.save(any(Coach.class))).thenReturn(testCoach);

            Coach result = coachService.updateCoachStatus(1L, CoachStatus.ACTIVE);

            assertThat(result.getCoachStatus()).isEqualTo(CoachStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("deleteCoach")
    class DeleteCoach {

        @Test
        @DisplayName("should delete coach successfully")
        void shouldDeleteCoach() {
            when(coachRepository.findById(1L)).thenReturn(Optional.of(testCoach));
            doNothing().when(coachRepository).delete(testCoach);

            coachService.deleteCoach(1L);

            verify(coachRepository).delete(testCoach);
        }

        @Test
        @DisplayName("should throw exception when deleting non-existent coach")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            when(coachRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> coachService.deleteCoach(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
