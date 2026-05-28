package com.sprint.mission.monew.domain.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record NotificationQueryCondition(
    UUID cursor,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    Instant after,

    @NotNull(message = "limit은 필수입니다")
    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 100, message = "limit must not exceed 100")
    Integer limit
) {

  public boolean hasCursor() {
    return cursor != null && after != null;
  }
}
