package com.nowcoder.community.common.kafka;

import com.nowcoder.community.domain.entity.DiscussNotice;
import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.LikeNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.po.DiscussPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikePublisher {

    private final KafkaTemplate<String, EventNotice> kafkaTemplate;
    private static final String LIKE_TOPIC = "like.notice";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(LikeNotice likeNotice) {
        //  在这里可以对消息进行处理，由于消息在整个过程中已经被封装好了，这里就不需要处理
        EventNotice eventNotice = EventNotice.builder()
                .id(UUID.randomUUID().toString())
                .version("1.0")
                .createTime(LocalDateTime.now())
                .extra(likeNotice)
                .attributes(null)
                .build();
        sendAsync(eventNotice);
    }

    /**
     * 根据点赞人id作为key发送消息，保证同一用户的消息有序，不会出现点赞后取消的情况没有按顺序完成，进行消息分组
     * @param eventNotice
     */
    private void sendAsync(EventNotice eventNotice) {
        NoticePayload noticePayload = eventNotice.getExtra();
        if (noticePayload instanceof LikeNotice likeNotice) {
            kafkaTemplate.send(LIKE_TOPIC, String.valueOf(likeNotice.getUserId()), eventNotice)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("消息发送失败: {}", ex.getMessage());
                        } else {
                            String message = "消息发送成功" + "  " +
                                    "操作类型为" + likeNotice.getLikeStatus() + " " +
                                    "发布者id为" + likeNotice.getUserId() + " " +
                                    "目标类型为" + likeNotice.getTargetType() + "  " +
                                    "目标id为" + likeNotice.getTargetId() + " " +
                                    "创建时间为" + likeNotice.getCreateTime() + " ";
                            log.info(message);
                        }
                    });
        } else {
            log.error("消息格式有误: {},消息类型为{}", eventNotice, noticePayload.getClass());
        }
    }
}
