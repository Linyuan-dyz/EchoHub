package com.nowcoder.community.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("discuss_post")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscussPost {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private int userId;

    private String title;

    private String content;

    @Builder.Default
    private int type = 0;

    @Builder.Default
    private int status = 0;

//    private int likeCount;

    private LocalDateTime createTime;

    @Builder.Default
    private int commentCount = 0;

    @Builder.Default
    private int score = 0;
}
