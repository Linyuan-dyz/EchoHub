package com.nowcoder.community.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyContentVO {

    Integer rpid;

    Integer objId;

    Integer rootRpid;

    Integer parentRpid;

    Integer likeCount;

    Integer replyCount;

    Integer floor;

    Integer userId;

    LocalDateTime createTime;

    String content;

    Integer version;
}
