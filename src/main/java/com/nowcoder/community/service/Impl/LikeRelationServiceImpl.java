package com.nowcoder.community.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.domain.po.LikeRelation;
import com.nowcoder.community.mapper.LikeRelationMapper;
import com.nowcoder.community.service.LikeRelationService;
import org.springframework.stereotype.Service;

@Service
public class LikeRelationServiceImpl extends ServiceImpl<LikeRelationMapper, LikeRelation> implements LikeRelationService {
}
