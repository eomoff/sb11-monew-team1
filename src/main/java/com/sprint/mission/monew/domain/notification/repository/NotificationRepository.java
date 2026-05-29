package com.sprint.mission.monew.domain.notification.repository;

import com.sprint.mission.monew.domain.notification.entity.Notification;
import com.sprint.mission.monew.domain.notification.repository.querydsl.NotificationCustomRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
    NotificationCustomRepository {

  long countByUserIdAndConfirmedAtIsNull(UUID userId);
}