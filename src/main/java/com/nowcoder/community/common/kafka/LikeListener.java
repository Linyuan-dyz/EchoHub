package com.nowcoder.community.common.kafka;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.LikeNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.po.LikeRelation;
import com.nowcoder.community.mapper.LikeRelationMapper;
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
public class LikeListener {

    private static final String LIKE_TOPIC = "like.notice";
    private final LikeRelationMapper likeRelationMapper;

    @KafkaListener(topics = LIKE_TOPIC)
    public void onMessage(@Payload EventNotice eventNotice,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.OFFSET) long offset) {
        NoticePayload payload = eventNotice.getExtra();
        if (payload instanceof LikeNotice likeNotice) {
            LikeNotice.OperateType operateType = likeNotice.getOperateType();
            //  点赞情况处理，需要向数据库内添加数据
            if (operateType.equals(LikeNotice.OperateType.CREATE)) {
                LikeRelation likeRelation = LikeRelation.builder()
                        .userId(likeNotice.getUserId())
                        .targetType(likeNotice.getTargetType())
                        .targetId(likeNotice.getTargetId())
                        .createTime(likeNotice.getCreateTime())
                        .build();
                //  TODO：可以想办法批量插入数据库
                likeRelationMapper.insert(likeRelation);
                log.info("点赞帖子成功：{}", likeNotice);
            } else {
                //  取消点赞处理，需要删除数据库的点赞数据
                QueryWrapper<LikeRelation> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", likeNotice.getUserId());
                queryWrapper.eq("target_type", likeNotice.getTargetType());
                queryWrapper.eq("target_id", likeNotice.getTargetId());
                //  TODO:批量删除
                likeRelationMapper.delete(queryWrapper);
                log.info("取消点赞帖子成功：{}", likeNotice);
            }
        }
    }
}
