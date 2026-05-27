package com.sprint.mission.monew.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", updatable = false)
  private UUID id;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Column(nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_type", nullable = false)
  private ResourceType resourceType;

  @Column(name = "resource_id", nullable = false, columnDefinition = "uuid")
  private UUID resourceId;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private Notification(
      UUID userId,
      String content,
      ResourceType resourceType,
      UUID resourceId
  ) {
    this.userId = userId;
    this.content = content;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  public static Notification create(
      UUID userId,
      String content,
      ResourceType resourceType,
      UUID resourceId
  ) {
    return new Notification(
        userId,
        content,
        resourceType,
        resourceId
    );
  }

  public boolean isConfirmed() {
    return confirmedAt != null;
  }

  public void confirm() {
    if (!isConfirmed()) {
      confirmedAt = Instant.now();
    }
  }
}
