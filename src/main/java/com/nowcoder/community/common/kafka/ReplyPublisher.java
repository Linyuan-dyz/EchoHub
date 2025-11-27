package com.nowcoder.community.common.kafka;

import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.LikeNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.entity.ReplyNotice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReplyPublisher {

    private final KafkaTemplate<String, EventNotice> kafkaTemplate;
    private static final String REPLY_SECTION_TOPIC = "reply.section.notice";
    private static final String REPLY_INDEX_TOPIC = "reply.index.notice";
    private static final String REPLY_DETAILS_TOPIC = "reply.details.notice";

    //  重建评论区基础信息缓存的消息发布
    public void publishReconstructReplySection(int discussPostId) {
        //  在这里可以对消息进行处理，由于消息在整个过程中已经被封装好了，这里就不需要处理
        EventNotice eventNotice = EventNotice.builder()
                .id(UUID.randomUUID().toString())
                .version("1.0")
                .createTime(LocalDateTime.now())
                .extra(new ReplyNotice(discussPostId, null))
                .attributes(null)
                .build();
        sendAsync(eventNotice, REPLY_SECTION_TOPIC);
    }

    //  重建评论区评论关系缓存的消息发布
    public void publishReconstructReplyIndex(int discussPostId) {
        EventNotice eventNotice = EventNotice.builder()
                .id(UUID.randomUUID().toString())
                .version("1.0")
                .createTime(LocalDateTime.now())
                .extra(new ReplyNotice(discussPostId, null))
                .attributes(null)
                .build();
        sendAsync(eventNotice, REPLY_INDEX_TOPIC);
    }

    public void publishReconstructReplyContent(int reid) {
        EventNotice eventNotice = EventNotice.builder()
                .id(UUID.randomUUID().toString())
                .version("1.0")
                .createTime(LocalDateTime.now())
                .extra(new ReplyNotice(null, reid))
                .attributes(null)
                .build();
        sendAsync(eventNotice, REPLY_DETAILS_TOPIC);
    }

    /**
     * 根据帖子id或评论id投递分区，保证消息消费的顺序性和重建缓存操作的唯一性
     * @param eventNotice
     */
    private void sendAsync(EventNotice eventNotice, String topic) {
        NoticePayload noticePayload = eventNotice.getExtra();
        if (noticePayload instanceof ReplyNotice replySectionNotice) {
            Integer discussPostId = replySectionNotice.getDiscussPostId();
            Integer replyId = replySectionNotice.getReplyId();
            kafkaTemplate.send(topic, String.valueOf(discussPostId == null ? replyId : discussPostId), eventNotice)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("消息发送失败: {}", ex.getMessage());
                        } else {
                            String message = "消息发送成功" + "  " +
                                    "评论区id为" + replySectionNotice.getDiscussPostId() + " " +
                                    "帖子id为" + replySectionNotice.getReplyId() + " ";
                            log.info(message);
                        }
                    });
        } else {
            log.error("消息格式有误: {},消息类型为{}", eventNotice, noticePayload.getClass());
        }
    }
}
