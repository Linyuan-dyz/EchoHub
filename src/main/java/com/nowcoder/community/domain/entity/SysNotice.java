package com.nowcoder.community.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysNotice implements NoticePayload {
    // 事件类型
    EventType eventType;
    // 行为发起者（点赞/评论的人）
    int actorUserId;
    // 被通知的用户ID（例如帖子作者）
    int targetUserId;
    // 对象类型（帖子/评论）
    ObjectType objectType;
    // 对象ID（postId/commentId）
    int objectId;

    public enum ObjectType {
        POST, COMMENT
    }

    public enum EventType {
        LIKE, COMMENT; // future: FOLLOW, MENTION, REPLY, SYSTEM_ALERT ...
        @JsonCreator
        public static EventType from(String value) {
            if (value == null) return null;
            return EventType.valueOf(value.trim().toUpperCase());
        }
    }
}
