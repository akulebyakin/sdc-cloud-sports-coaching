package com.kulebiakin.sportscoaching.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coach")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coach_id")
    private Long coachId;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0")
    @DecimalMax(value = "10.0", message = "Rating must not exceed 10")
    @Column(name = "rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "strike_count")
    @Builder.Default
    private Integer strikeCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "coach_status", nullable = false, length = 20)
    @Builder.Default
    private CoachStatus coachStatus = CoachStatus.ACTIVE;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Session> sessions = new ArrayList<>();
}
