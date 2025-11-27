package com.nowcoder.community.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jdk.jfr.Description;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comment")
public class Comment {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private int userId;

    private int entityType;

    @Description("帖子id")
    private int entityId;

    @Description("回复用户id")
    @Builder.Default
    private int targetId = 0;

//    private int likeCount;

    private String content;

    private int status;

    private LocalDateTime createTime;
}
