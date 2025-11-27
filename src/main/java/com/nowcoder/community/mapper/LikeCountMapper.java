package com.nowcoder.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.domain.po.LikeCount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeCountMapper extends BaseMapper<LikeCount> {
    @Insert({
            "<script>",
            "INSERT INTO like_count (business_type, target_id, like_count, dislike_count) VALUES",
            "<foreach collection='list' item='e' separator=','>",
            "(#{e.businessType}, #{e.targetId}, #{e.likeCount}, #{e.dislikeCount})",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE",
            "like_count = VALUES(like_count),",
            "dislike_count = VALUES(dislike_count)",
            "</script>"
    })
    int upsertDeltaBatch(@Param("list") List<LikeCount> list);
}
