package com.sprint.mission.monew.domain.notification.controller;

import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(summary = "미확인 알림 목록 조회 (커서 페이지네이션)")
  @GetMapping
  public ResponseEntity<CursorPageResponse<NotificationResponse>> findUnconfirmed(
      @Parameter(hidden = true) @RequestHeader(HeaderNames.REQUEST_USER_ID) UUID userId,
      @Valid @ModelAttribute NotificationSearchRequest request
  ) {
    return ResponseEntity.ok(notificationService.findUnconfirmed(userId, request));
  }

}
