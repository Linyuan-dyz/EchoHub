package com.nowcoder.community.common.kafka;

import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.entity.SysNotice;
import com.nowcoder.community.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final MessageService messageService;

    private static final String TOPIC = "post.notice";

    @KafkaListener(topics = TOPIC)
    public void onMessage(@Payload EventNotice notice,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.OFFSET) long offset) {
        //  TODO:redis实现消息幂等性
        NoticePayload noticePayload = notice.getExtra();
        if (noticePayload instanceof SysNotice sysNotice) {
            //  实现消息类型的分发处理，在这里先将消息保存到数据库中，后续可以拓展
            switch (sysNotice.getEventType()) {
                case LIKE:
                    handleLikeNotice(notice);
                    break;
                case COMMENT:
                    handleCommentNotice(notice);
                    break;
                default:
                    log.error("暂时不支持的消息类型: {}", sysNotice.getEventType());
            }

//        // 保存系统通知到数据库
//        messageService.saveSystemMessage(notice);
        } else {
            log.error("消息格式有误: {},消息类型为{}", notice, noticePayload.getClass());
        }
    }

    private void handleCommentNotice(EventNotice notice) {
        messageService.saveSystemMessage(notice);
    }

    private void handleLikeNotice(EventNotice notice) {
        messageService.saveSystemMessage(notice);
    }
}
