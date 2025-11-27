package com.nowcoder.community.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("like_count")
public class LikeCount {

    @TableId(value = "id", type = IdType.AUTO)
    int id;

    // 业务ID（帖子ID/评论ID），0表示帖子，1表示评论
    @TableField("business_type")
    private Integer businessType;

    @TableField("target_id")
    private Integer targetId;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("dislike_count")
    private Integer dislikeCount;
}
