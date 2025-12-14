package com.kulebiakin.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachStatusRequest {

    @NotNull(message = "Coach ID is required")
    private Long coachId;

    @NotNull(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|DEACTIVATED)$", message = "Status must be ACTIVE or DEACTIVATED")
    private String status;
}
