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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("reply_index")
public class ReplyIndex {
    @TableId(value = "rpid", type = IdType.AUTO)
    Integer rpid;

    @TableField(value = "obj_id")
    Integer objId;

    @TableField(value = "root_id")
    Integer rootRpid;

    @TableField(value = "parent_id")
    Integer parentRpid;

    @TableField(value = "like_count")
    Integer likeCount;

    @TableField(value = "reply_count")
    Integer replyCount;

    @TableField(value = "floor")
    Integer floor;

    @TableField(value = "user_id")
    Integer userId;

    @TableField(value = "created_time")
    LocalDateTime createTime;
}
