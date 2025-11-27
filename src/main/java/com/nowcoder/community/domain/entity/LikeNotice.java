package com.nowcoder.community.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LikeNotice implements NoticePayload {

    public OperateType operateType;

    public LikeStatus likeStatus;

    public int userId;

    public int targetId;

    public int targetType;

    public LocalDateTime createTime;

    public enum OperateType {
        CREATE, CANCEL;
    }
}
