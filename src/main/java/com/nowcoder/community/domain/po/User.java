package com.nowcoder.community.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Builder
public class User {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    public int id;

    public String username;

    public String password;

    public String salt;

    public String phone;

    public String email;

    public int type;

    public int status;

    public String activationCode;

    public String headerUrl;

    @JsonIgnore
    public LocalDateTime createTime;

}
