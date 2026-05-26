package com.sprint.mission.monew.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String content;

  @Column(name = "resource_type", nullable = false)
  private String resourceType;

  @Column(name = "resource_id", nullable = false)
  private UUID resourceId;

  // Null 관련 확인 필요
  @Column(name = "confirmed_at")
  private LocalDateTime confirmedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  private Notification(
      User user,
      String content,
      String resourceType,
      UUID resourceId,
      LocalDateTime createdAt
  ) {
    this.user = user;
    this.content = content;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.confirmedAt = null;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public static Notification create(
      User user,
      String content,
      String resourceType,
      UUID resourceId
  ) {
    return new Notification(
        user,
        content,
        resourceType,
        resourceId,
        LocalDateTime.now());
  }

  // null이 아니면 true 반환
  public boolean isConfirmed() {
    return confirmedAt != null;
  }

  public void confirm() {
    this.confirmedAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }
}
