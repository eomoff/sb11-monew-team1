package com.sprint.mission.monew.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.monew.domain.notification.entity.Notification;
import com.sprint.mission.monew.domain.notification.entity.ResourceType;
import com.sprint.mission.monew.domain.notification.repository.NotificationRepository;
import com.sprint.mission.monew.domain.user.entity.User;
import com.sprint.mission.monew.domain.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class NotificationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  private User user;

  @BeforeEach
  void setUp() {
    user = userRepository.save(User.create("notify@test.com", "알림테스트유저", "password123!"));
  }

  @Nested
  @DisplayName("GET /api/notifications — 미확인 알림 목록 조회")
  class FindUnconfirmed {

    @Test
    @DisplayName("미확인 알림 목록을 반환한다")
    void 미확인_알림_목록을_반환한다() throws Exception {
      // given
      notificationRepository.save(
          Notification.create(user.getId(), "알림1", ResourceType.INTEREST, UUID.randomUUID()));
      notificationRepository.save(
          Notification.create(user.getId(), "알림2", ResourceType.INTEREST, UUID.randomUUID()));

      // when & then
      mockMvc.perform(get("/api/notifications")
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(2))
          .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("확인된 알림은 목록에 포함되지 않는다")
    void 확인된_알림은_목록에_포함되지_않는다() throws Exception {
      // given
      notificationRepository.save(
          Notification.create(user.getId(), "미확인", ResourceType.INTEREST, UUID.randomUUID()));
      Notification confirmed = notificationRepository.save(
          Notification.create(user.getId(), "확인됨", ResourceType.INTEREST, UUID.randomUUID()));
      confirmed.confirm();
      notificationRepository.save(confirmed);

      // when & then
      mockMvc.perform(get("/api/notifications")
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Monew-Request-User-ID 헤더가 없으면 400을 반환한다")
    void 헤더가_없으면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/notifications"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PATCH /api/notifications/{notificationId} — 알림 단건 확인")
  class Confirm {

    @Test
    @DisplayName("알림 단건 확인 성공 시 DB에 confirmedAt이 설정된다")
    void 알림_단건_확인_성공_시_DB에_confirmedAt이_설정된다() throws Exception {
      // given
      Notification notification = notificationRepository.save(
          Notification.create(user.getId(), "알림", ResourceType.INTEREST, UUID.randomUUID()));

      // when & then
      mockMvc.perform(patch("/api/notifications/{notificationId}", notification.getId())
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isNoContent());

      Notification confirmed = notificationRepository.findById(notification.getId()).orElseThrow();
      assertThat(confirmed.isConfirmed()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 알림 확인 시 404를 반환한다")
    void 존재하지_않는_알림_확인_시_404를_반환한다() throws Exception {
      // when & then
      mockMvc.perform(patch("/api/notifications/{notificationId}", UUID.randomUUID())
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("타인의 알림 확인 시 404를 반환한다")
    void 타인의_알림_확인_시_404를_반환한다() throws Exception {
      // given
      User other = userRepository.save(User.create("other@test.com", "타인", "password123!"));
      Notification notification = notificationRepository.save(
          Notification.create(other.getId(), "타인 알림", ResourceType.INTEREST, UUID.randomUUID()));

      // when & then
      mockMvc.perform(patch("/api/notifications/{notificationId}", notification.getId())
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Monew-Request-User-ID 헤더가 없으면 400을 반환한다")
    void 헤더가_없으면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(patch("/api/notifications/{notificationId}", UUID.randomUUID()))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PATCH /api/notifications — 알림 전체 확인")
  class ConfirmAll {

    @Test
    @DisplayName("알림 전체 확인 성공 시 DB의 모든 미확인 알림에 confirmedAt이 설정된다")
    void 알림_전체_확인_성공_시_DB의_모든_미확인_알림에_confirmedAt이_설정된다() throws Exception {
      // given
      notificationRepository.save(
          Notification.create(user.getId(), "알림1", ResourceType.INTEREST, UUID.randomUUID()));
      notificationRepository.save(
          Notification.create(user.getId(), "알림2", ResourceType.COMMENT, UUID.randomUUID()));

      // when & then
      mockMvc.perform(patch("/api/notifications")
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isNoContent());

      long unconfirmedCount = notificationRepository.countByUserIdAndConfirmedAtIsNull(user.getId());
      assertThat(unconfirmedCount).isZero();
    }

    @Test
    @DisplayName("이미 확인된 알림은 전체 확인 후에도 confirmedAt이 변경되지 않는다")
    void 이미_확인된_알림은_전체_확인_후에도_confirmedAt이_변경되지_않는다() throws Exception {
      // given
      Notification confirmed = notificationRepository.save(
          Notification.create(user.getId(), "확인됨", ResourceType.INTEREST, UUID.randomUUID()));
      confirmed.confirm();
      notificationRepository.save(confirmed);
      var originalConfirmedAt = confirmed.getConfirmedAt();

      // when
      mockMvc.perform(patch("/api/notifications")
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isNoContent());

      // then
      Notification reloaded = notificationRepository.findById(confirmed.getId()).orElseThrow();
      assertThat(reloaded.getConfirmedAt()).isEqualTo(originalConfirmedAt);
    }

    @Test
    @DisplayName("Monew-Request-User-ID 헤더가 없으면 400을 반환한다")
    void 헤더가_없으면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(patch("/api/notifications"))
          .andExpect(status().isBadRequest());
    }
  }
}