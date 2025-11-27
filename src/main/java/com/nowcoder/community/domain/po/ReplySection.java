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

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("subject")
public class ReplySection {
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;

    @TableField(value = "post_id")
    Integer postId;

    @TableField(value = "root_count")
    Integer rootCount;

    @TableField(value = "child_count")
    Integer childCount;

    @TableField(value = "total_count")
    Integer totalCount;

    @TableField(value = "version")
    Integer version;

    @TableField(value = "create_time")
    LocalDateTime createTime;
}
