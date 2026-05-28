package com.sprint.mission.monew.domain.notification.service;


import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.domain.notification.dto.NotificationResponse;
import com.sprint.mission.monew.domain.notification.dto.NotificationSearchRequest;
import com.sprint.mission.monew.domain.notification.entity.Notification;
import com.sprint.mission.monew.domain.notification.mapper.NotificationMapper;
import com.sprint.mission.monew.domain.notification.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

  static final int DEFAULT_LIMIT = 50;

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  public CursorPageResponse<NotificationResponse> findUnconfirmed(UUID userId,
      NotificationSearchRequest request) {
    int pageSize = resolvePageSize(request.limit());
    List<Notification> fetched = notificationRepository.findUnconfirmedSlice(
        userId, request.cursor(), request.after(), pageSize);

    boolean hasNext = fetched.size() > pageSize;
    List<Notification> page = hasNext ? fetched.subList(0, pageSize) : fetched;

    long totalElements = notificationRepository.countByUserIdAndConfirmedAtIsNull(userId);

    return notificationMapper.toCursorPage(page, hasNext, totalElements);
  }

  private int resolvePageSize(Integer limit) {
    return limit == null ? DEFAULT_LIMIT : limit;
  }
}