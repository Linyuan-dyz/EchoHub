package com.nowcoder.community.common.kafka;

import com.nowcoder.community.domain.entity.DiscussNotice;
import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.po.DiscussPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostPublisher {

    private static final String TOPIC = "discuss.esNotice";
    private final KafkaTemplate<String, EventNotice> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(DiscussNotice discussNotice) {
        //  在这里可以对消息进行处理，由于消息在整个过程中已经被封装好了，这里就不需要处理
        EventNotice eventNotice = EventNotice.builder()
                .id(UUID.randomUUID().toString())
                .version("1.0")
                .createTime(LocalDateTime.now())
                .extra(discussNotice)
                .attributes(null)
                .build();
        sendAsync(eventNotice);
    }

    /**
     * 根据帖子id作为key发送消息，保证同一用户的消息有序，进行消息分组
     * @param eventNotice
     */
    private void sendAsync(EventNotice eventNotice) {
        NoticePayload noticePayload = eventNotice.getExtra();
        if (noticePayload instanceof DiscussNotice discussNotice) {
            DiscussPost discussPost = discussNotice.getDiscuss();
            kafkaTemplate.send(TOPIC, String.valueOf(discussPost.getId()), eventNotice)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("消息发送失败: {}", ex.getMessage());
                        } else {
                            String message = "消息发送成功" + "  " +
                                    "帖子id为" + discussPost.getId() + "  " +
                                    "操作类型为" + discussNotice.getActionType() + "  " +
                                    "发布者id为" + discussPost.getUserId() + " " +
                                    "帖子标题为" + discussPost.getTitle() + " ";
                            log.info(message);
                        }
                    });
        } else {
            log.error("消息格式有误: {},消息类型为{}", eventNotice, noticePayload.getClass());
        }
    }
}
