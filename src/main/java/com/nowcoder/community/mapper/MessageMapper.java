package com.nowcoder.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.domain.po.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
