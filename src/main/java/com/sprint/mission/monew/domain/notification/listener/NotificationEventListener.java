package com.sprint.mission.monew.domain.notification.listener;

import com.sprint.mission.monew.domain.article.event.ArticleCreatedEvent;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import com.sprint.mission.monew.domain.interest.repository.SubscriptionRepository;
import com.sprint.mission.monew.domain.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final InterestRepository interestRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final NotificationService notificationService;

  @EventListener
  public void handleArticleCreated(ArticleCreatedEvent event) {
    String title = event.article().getTitle();
    String summary = event.article().getSummary();

    List<Interest> interests = interestRepository.findMatchingInterests(title, summary);
    if (interests.isEmpty()) {
      return;
    }

    for (Interest interest : interests) {
      List<UUID> subscriberIds = subscriptionRepository.findUserIdsByInterestId(interest.getId());
      notificationService.createArticleNotifications(interest.getId(), interest.getName(),
          subscriberIds);
    }
    log.info("기사 등록 이벤트 처리 완료: 매칭 관심사={}개, 기사 제목={}", interests.size(), title);
  }
}