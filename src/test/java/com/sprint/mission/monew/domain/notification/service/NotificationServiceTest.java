package com.sprint.mission.monew.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.domain.notification.dto.NotificationQueryCondition;
import com.sprint.mission.monew.domain.notification.dto.NotificationResponse;
import com.sprint.mission.monew.domain.notification.entity.Notification;
import com.sprint.mission.monew.domain.notification.entity.ResourceType;
import com.sprint.mission.monew.domain.notification.mapper.NotificationMapper;
import com.sprint.mission.monew.domain.notification.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks
  NotificationService notificationService;
  @Mock
  NotificationRepository notificationRepository;
  @Mock
  NotificationMapper notificationMapper;

  UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("미확인 알림 목록 조회")
  class FindUnconfirmed {

    @Test
    @DisplayName("limit이 null이면 DEFAULT_LIMIT(50)으로 조회한다")
    void limit이_null이면_DEFAULT_LIMIT으로_조회한다() {
      // given
      NotificationQueryCondition request = new NotificationQueryCondition(null, null, null);
      given(notificationRepository.findUnconfirmedSlice(eq(userId), any(), any(),
          eq(NotificationService.DEFAULT_LIMIT))).willReturn(List.of());
      given(notificationRepository.countByUserIdAndConfirmedAtIsNull(userId)).willReturn(0L);
      given(notificationMapper.toCursorPage(any(), eq(false), eq(0L)))
          .willReturn(new CursorPageResponse<>(List.of(), null, null, false, 0, 0L));

      // when
      notificationService.findUnconfirmed(userId, request);

      // then
      then(notificationRepository).should()
          .findUnconfirmedSlice(userId, null, null, NotificationService.DEFAULT_LIMIT);
    }

    @Test
    @DisplayName("limit이 지정되면 해당 값으로 조회한다")
    void limit이_지정되면_해당_값으로_조회한다() {
      // given
      NotificationQueryCondition request = new NotificationQueryCondition(null, null, 10);
      given(notificationRepository.findUnconfirmedSlice(eq(userId), any(), any(), eq(10)))
          .willReturn(List.of());
      given(notificationRepository.countByUserIdAndConfirmedAtIsNull(userId)).willReturn(0L);
      given(notificationMapper.toCursorPage(any(), eq(false), eq(0L)))
          .willReturn(new CursorPageResponse<>(List.of(), null, null, false, 0, 0L));

      // when
      notificationService.findUnconfirmed(userId, request);

      // then
      then(notificationRepository).should()
          .findUnconfirmedSlice(userId, null, null, 10);
    }

    @Test
    @DisplayName("다음 페이지가 있으면 hasNext=true로 mapper를 호출한다")
    void 다음_페이지가_있으면_hasNext_true로_mapper를_호출한다() {
      // given
      int limit = 2;
      NotificationQueryCondition request = new NotificationQueryCondition(null, null, limit);
      List<Notification> fetched = notifications(limit + 1);

      given(notificationRepository.findUnconfirmedSlice(eq(userId), any(), any(), eq(limit)))
          .willReturn(fetched);
      given(notificationRepository.countByUserIdAndConfirmedAtIsNull(userId)).willReturn(3L);
      given(notificationMapper.toCursorPage(fetched.subList(0, limit), true, 3L))
          .willReturn(new CursorPageResponse<>(List.of(), null, null, true, limit, 3L));

      // when
      CursorPageResponse<NotificationResponse> result =
          notificationService.findUnconfirmed(userId, request);

      // then
      then(notificationMapper).should()
          .toCursorPage(fetched.subList(0, limit), true, 3L);
      assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("마지막 페이지면 hasNext=false로 mapper를 호출한다")
    void 마지막_페이지면_hasNext_false로_mapper를_호출한다() {
      // given
      int limit = 10;
      NotificationQueryCondition request = new NotificationQueryCondition(null, null, limit);
      List<Notification> fetched = notifications(3);

      given(notificationRepository.findUnconfirmedSlice(eq(userId), any(), any(), eq(limit)))
          .willReturn(fetched);
      given(notificationRepository.countByUserIdAndConfirmedAtIsNull(userId)).willReturn(3L);
      given(notificationMapper.toCursorPage(fetched, false, 3L))
          .willReturn(new CursorPageResponse<>(List.of(), null, null, false, 3, 3L));

      // when
      CursorPageResponse<NotificationResponse> result =
          notificationService.findUnconfirmed(userId, request);

      // then
      then(notificationMapper).should().toCursorPage(fetched, false, 3L);
      assertThat(result.hasNext()).isFalse();
    }
  }

  private List<Notification> notifications(int count) {
    List<Notification> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(Notification.create(userId, "알림" + i, ResourceType.INTEREST, UUID.randomUUID()));
    }
    return list;
  }
}
