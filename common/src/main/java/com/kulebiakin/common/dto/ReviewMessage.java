package com.kulebiakin.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewMessage {
    private Long sessionId;
    private BigDecimal rating;
    private String comment;
    private LocalDateTime timestamp;
    private Long coachId;
}
