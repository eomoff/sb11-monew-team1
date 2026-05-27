package com.sprint.mission.monew.domain.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record NotificationSearchRequest(
    UUID cursor,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    Instant after,

    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 100, message = "limit must not exceed 100")
    Integer limit
) {

  public boolean hasCursor() {
    return cursor != null && after != null;
  }
}
