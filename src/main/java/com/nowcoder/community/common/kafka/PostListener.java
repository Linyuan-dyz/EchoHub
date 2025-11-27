package com.nowcoder.community.common.kafka;

import com.nowcoder.community.domain.entity.DiscussNotice;
import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.entity.PostDocument;
import com.nowcoder.community.domain.po.DiscussPost;
import com.nowcoder.community.service.Impl.PostServiceImpl;
import com.nowcoder.community.service.PostService;
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
public class PostListener {

    private static final String TOPIC = "discuss.esNotice";
    private final PostService postService;

    @KafkaListener(topics = TOPIC)
    public void onMessage(@Payload EventNotice eventNotice,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.OFFSET) long offset) {
        NoticePayload noticePayload = eventNotice.getExtra();
        if (noticePayload instanceof DiscussNotice discussNotice) {
            DiscussNotice.ActionType actionType = discussNotice.getActionType();
            DiscussPost discussPost = discussNotice.getDiscuss();
            PostDocument postDocument = PostDocument.builder()
                    .id(discussPost.getId())
                    .userId(discussPost.getUserId())
                    .title(discussPost.getTitle())
                    .content(discussPost.getContent())
                    .type(discussPost.getType())
                    .status(discussPost.getStatus())
                    .createTime(discussPost.getCreateTime())
                    .commentCount(discussPost.getCommentCount())
                    .score(discussPost.getScore())
                    .build();
            switch (actionType) {
                case CREATED -> {
                    postService.create(postDocument);
                    log.info("成功创建帖子文档{}", postDocument);
                }
                case UPDATED -> {
                    postService.update(postDocument);
                    log.info("成功更新帖子文档{}", postDocument);
                }
                case DELETED -> {
                    postService.delete(postDocument.getId());
                    log.info("成功删除帖子文档{}", postDocument.getId());
                }
                default -> {
                    log.error("操作类型有误: {}", actionType);
                }
            }
        } else {
            log.error("消息格式有误: {},消息类型为{}", eventNotice, noticePayload.getClass());
        }
    }
}
