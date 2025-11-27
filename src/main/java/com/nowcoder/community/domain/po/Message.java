package com.nowcoder.community.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("message")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    public static final int MESSAGE_UNREAD = 0;
    public static final int MESSAGE_READ = 1;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    public int id;

    @TableField(value = "from_id")
    public int fromId;

    @TableField(value = "to_id")
    public int toId;

    public String conversationId;

    public String content;

    public int status;

    public LocalDateTime createTime;
}
