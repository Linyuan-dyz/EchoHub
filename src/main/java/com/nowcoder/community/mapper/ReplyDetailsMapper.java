package com.nowcoder.community.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.domain.vo.ReplyContentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface ReplyDetailsMapper extends BaseMapper<ReplyContentVO> {

    List<ReplyContentVO> listByRpidIn(@Param("ids") List<Integer> ids);
}
