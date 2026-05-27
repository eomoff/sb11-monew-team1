package com.sprint.mission.monew.domain.notification.repository;

import com.sprint.mission.monew.domain.notification.entity.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final EntityManager em;

  @Override
  public List<Notification> findUnconfirmedSlice(UUID userId, UUID cursorId,
      Instant cursorCreatedAt, int limit) {
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
        .setMaxResults(limit + 1);

    if (!firstPage) {
      query.setParameter("cursorCreatedAt", cursorCreatedAt);
      query.setParameter("cursorId", cursorId);
    }

    return query.getResultList();
  }
}
