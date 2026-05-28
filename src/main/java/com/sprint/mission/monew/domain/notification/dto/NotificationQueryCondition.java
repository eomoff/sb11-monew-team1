package com.sprint.mission.monew.domain.notification.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record NotificationQueryCondition(
    UUID cursor,

    Instant after,

    @NotNull(message = "limit은 필수입니다")
    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 100, message = "limit must not exceed 100")
    Integer limit
) {

  public boolean hasCursor() {
    return cursor != null && after != null;
  }

  @AssertTrue(message = "cursor와 after는 함께 입력해야 합니다")
  public boolean isCursorPaired() {
    return (cursor == null) == (after == null);
  }
}
