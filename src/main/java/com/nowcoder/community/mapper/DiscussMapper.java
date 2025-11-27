package com.nowcoder.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.domain.po.DiscussPost;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiscussMapper extends BaseMapper<DiscussPost> {
}
