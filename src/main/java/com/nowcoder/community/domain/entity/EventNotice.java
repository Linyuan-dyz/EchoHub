package com.nowcoder.community.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventNotice {
    // 事件唯一ID（幂等用）
    String id;
    // 契约版本，方便前后消息类版本识别
    String version;
    // 事件发生时间
    LocalDateTime createTime;
    // 事件内容，不同事件类型对应不同的内容
    NoticePayload extra;
    // 额外字段（前向兼容/扩展），可以在适配不同类型消息时直接在这里填入，不需要修改消息类
    Map<String, Object> attributes;
}
