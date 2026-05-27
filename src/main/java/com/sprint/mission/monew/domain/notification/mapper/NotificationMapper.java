package com.sprint.mission.monew.domain.notification.mapper;

import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.domain.notification.dto.NotificationResponse;
import com.sprint.mission.monew.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  NotificationResponse toResponse(Notification notification);

  default CursorPageResponse<NotificationResponse> toCursorPage(
      List<Notification> page,
      boolean hasNext,
      long totalElements
  ) {
    List<NotificationResponse> content = page.stream()
        .map(this::toResponse)
        .toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !page.isEmpty()) {
      Notification last = page.get(page.size() - 1);
      nextCursor = last.getId().toString();
      nextAfter = last.getCreatedAt();
    }

    return CursorPageResponse.of(content, nextCursor, nextAfter, hasNext, content.size(),
        totalElements);
  }
}