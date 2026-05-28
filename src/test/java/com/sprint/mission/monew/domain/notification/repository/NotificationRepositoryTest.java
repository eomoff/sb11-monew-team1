package com.sprint.mission.monew.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.monew.common.config.JpaConfig;
import com.sprint.mission.monew.domain.notification.entity.Notification;
import com.sprint.mission.monew.domain.notification.entity.ResourceType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, NotificationRepositoryImpl.class})
class NotificationRepositoryTest {

  @Autowired
  NotificationRepository notificationRepository;

  private UUID userId;

  @BeforeEach
  void setUp() {
    notificationRepository.deleteAll();
    userId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("findUnconfirmedSlice")
  class FindUnconfirmedSlice {

    @Test
    @DisplayName("커서 없이 조회하면 해당 사용자의 미확인 알림만 반환한다")
    void 커서_없이_조회하면_해당_사용자의_미확인_알림만_반환한다() {
      // given
      notificationRepository.save(
          Notification.create(userId, "알림1", ResourceType.INTEREST, UUID.randomUUID()));
      notificationRepository.save(
          Notification.create(userId, "알림2", ResourceType.INTEREST, UUID.randomUUID()));

      // when
      List<Notification> result = notificationRepository.findUnconfirmedSlice(
          userId, null, null, 10);

      // then
      assertThat(result).hasSize(2);
      assertThat(result).allMatch(n -> n.getUserId().equals(userId));
      assertThat(result).allMatch(n -> !n.isConfirmed());
    }

    @Test
    @DisplayName("확인된 알림은 조회 결과에 포함되지 않는다")
    void 확인된_알림은_조회_결과에_포함되지_않는다() {
      // given
      Notification unconfirmed = notificationRepository.save(
          Notification.create(userId, "미확인", ResourceType.INTEREST, UUID.randomUUID()));
      Notification confirmed = notificationRepository.save(
          Notification.create(userId, "확인됨", ResourceType.INTEREST, UUID.randomUUID()));
      confirmed.confirm();
      notificationRepository.save(confirmed);

      // when
      List<Notification> result = notificationRepository.findUnconfirmedSlice(
          userId, null, null, 10);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(unconfirmed.getId());
    }

    @Test
    @DisplayName("다른 사용자의 알림은 조회 결과에 포함되지 않는다")
    void 다른_사용자의_알림은_조회_결과에_포함되지_않는다() {
      // given
      notificationRepository.save(
          Notification.create(userId, "내 알림", ResourceType.INTEREST, UUID.randomUUID()));
      notificationRepository.save(
          Notification.create(UUID.randomUUID(), "타인 알림", ResourceType.INTEREST,
              UUID.randomUUID()));

      // when
      List<Notification> result = notificationRepository.findUnconfirmedSlice(
          userId, null, null, 10);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("limit+1개를 반환해 다음 페이지 존재 여부를 판단할 수 있다")
    void limit_초과_데이터가_있으면_limit_더하기_1개를_반환한다() {
      // given
      for (int i = 0; i < 3; i++) {
        notificationRepository.save(
            Notification.create(userId, "알림" + i, ResourceType.INTEREST, UUID.randomUUID()));
      }

      // when
      List<Notification> result = notificationRepository.findUnconfirmedSlice(
          userId, null, null, 2);

      // then
      assertThat(result).hasSize(3);
    }
  }

  @Nested
  @DisplayName("countByUserIdAndConfirmedAtIsNull")
  class CountByUserIdAndConfirmedAtIsNull {

    @Test
    @DisplayName("미확인 알림 수를 반환한다")
    void 미확인_알림_수를_반환한다() {
      // given
      notificationRepository.save(
          Notification.create(userId, "알림1", ResourceType.INTEREST, UUID.randomUUID()));
      notificationRepository.save(
          Notification.create(userId, "알림2", ResourceType.INTEREST, UUID.randomUUID()));

      // when
      long count = notificationRepository.countByUserIdAndConfirmedAtIsNull(userId);

      // then
      assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("확인된 알림은 카운트에 포함되지 않는다")
    void 확인된_알림은_카운트에_포함되지_않는다() {
      // given
      notificationRepository.save(
          Notification.create(userId, "미확인", ResourceType.INTEREST, UUID.randomUUID()));
      Notification confirmed = notificationRepository.save(
          Notification.create(userId, "확인됨", ResourceType.INTEREST, UUID.randomUUID()));
      confirmed.confirm();
      notificationRepository.save(confirmed);

      // when
      long count = notificationRepository.countByUserIdAndConfirmedAtIsNull(userId);

      // then
      assertThat(count).isEqualTo(1);
    }
  }
}
