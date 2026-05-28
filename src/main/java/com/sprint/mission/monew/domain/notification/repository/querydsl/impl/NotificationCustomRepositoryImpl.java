package com.sprint.mission.monew.domain.notification.repository.querydsl.impl;

import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.domain.notification.dto.NotificationQueryCondition;
import com.sprint.mission.monew.domain.notification.dto.NotificationResponse;
import com.sprint.mission.monew.domain.notification.entity.Notification;
import com.sprint.mission.monew.domain.notification.mapper.NotificationMapper;
import com.sprint.mission.monew.domain.notification.repository.querydsl.NotificationCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

  private final EntityManager em;
  private final NotificationMapper notificationMapper;

  @Override
  public CursorPageResponse<NotificationResponse> findUnconfirmed(UUID userId,
      NotificationQueryCondition condition) {
    int pageSize = condition.limit();
    UUID cursorId = condition.cursor();
    Instant cursorCreatedAt = condition.after();
    boolean firstPage = (cursorId == null || cursorCreatedAt == null);

    String jpql = """
        SELECT n FROM Notification n
         WHERE n.userId = :userId
           AND n.confirmedAt IS NULL
        """
        + (firstPage ? "" : """
           AND (n.createdAt < :cursorCreatedAt
                OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
        """)
        + " ORDER BY n.createdAt DESC, n.id DESC";

    TypedQuery<Notification> query = em.createQuery(jpql, Notification.class)
        .setParameter("userId", userId)
        .setMaxResults(pageSize + 1);

    if (!firstPage) {
      query.setParameter("cursorCreatedAt", cursorCreatedAt);
      query.setParameter("cursorId", cursorId);
    }

    List<Notification> fetched = query.getResultList();
    boolean hasNext = fetched.size() > pageSize;
    List<Notification> page = hasNext ? fetched.subList(0, pageSize) : fetched;

    List<NotificationResponse> content = page.stream()
        .map(notificationMapper::toResponse)
        .toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !page.isEmpty()) {
      Notification last = page.get(page.size() - 1);
      nextCursor = last.getId().toString();
      nextAfter = last.getCreatedAt();
    }

    long totalElements = em.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.confirmedAt IS NULL",
            Long.class)
        .setParameter("userId", userId)
        .getSingleResult();

    return CursorPageResponse.of(content, nextCursor, nextAfter, hasNext, content.size(),
        totalElements);
  }
}