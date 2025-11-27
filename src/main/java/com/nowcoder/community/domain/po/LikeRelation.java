package com.nowcoder.community.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("like_relation")
public class LikeRelation {

    public static final int COMMENT_TYPE = 1;
    public static final int DISCUSS_POST_TYPE = 2;

    @TableId(value = "id", type = IdType.AUTO)
    public int id;

    @TableField("user_id")
    public int userId;

    @TableField("target_id")
    public int targetId;

    @TableField("target_type")
    public int targetType;

    @TableField("create_time")
    public LocalDateTime createTime;

}
