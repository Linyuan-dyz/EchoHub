package com.nowcoder.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.domain.po.LikeRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeRelationMapper extends BaseMapper<LikeRelation> {

    @Delete({
            "<script>",
            "DELETE FROM like_relation",
            "<if test='list != null and list.size() > 0'>",
            " WHERE ",
            "<foreach collection='list' item='item' separator=' OR '>",
            "(user_id = #{item.userId} AND target_id = #{item.targetId} AND target_type = #{item.targetType})",
            "</foreach>",
            "</if>",
            "</script>"
    })
    int batchDeleteByConditions(@Param("list") List<LikeRelation> condList);
}
