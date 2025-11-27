package com.nowcoder.community.common.kafka;

import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.entity.SysNotice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, EventNotice> kafkaTemplate;
    private static final String TOPIC = "post.notice";


    public void publishLike(int actorUserId, int targetUserId,
                            SysNotice.ObjectType objectType, int objectId,
                            SysNotice.EventType eventType,
                            @Nullable Map<String, Object> attributes) {
        SysNotice sysNotice = SysNotice.builder()
                .actorUserId(actorUserId)
                .targetUserId(targetUserId)
                .eventType(eventType)
                .objectType(objectType)
                .objectId(objectId)
                .build();
        EventNotice notice = EventNotice.builder()
                .id(UUID.randomUUID().toString())
                .version("1.0")
                .createTime(LocalDateTime.now())
                .extra(sysNotice)
                .attributes(attributes)
                .build();
        sendAsync(notice);
    }

    /**
     * 根据目标用户id作为key发送消息，保证同一用户的消息有序，进行消息分组
     * @param notice
     */
    private void sendAsync(EventNotice notice) {
        NoticePayload noticePayload = notice.getExtra();
        if (noticePayload instanceof SysNotice sysNotice) {
            kafkaTemplate.send(TOPIC, String.valueOf(sysNotice.getTargetUserId()), notice)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("消息发送失败: {}", ex.getMessage());
                        } else {
                            String message = "消息发送成功" + "  " +
                                    "消息类型为" + sysNotice.getEventType() + "  " +
                                    "目标用户id为" + sysNotice.getTargetUserId() + "  " +
                                    "消息来源为" + sysNotice.getActorUserId();
                            log.info(message);
                        }
                    });
        } else {
            log.error("消息格式有误: {},消息类型为{}", notice, noticePayload.getClass());
        }
    }
}
