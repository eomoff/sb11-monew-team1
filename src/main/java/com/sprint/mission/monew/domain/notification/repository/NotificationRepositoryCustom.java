package com.sprint.mission.monew.domain.notification.repository;

import com.sprint.mission.monew.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

  List<Notification> findUnconfirmedSlice(UUID userId, UUID cursorId, Instant cursorCreatedAt,
      int limit);

}
