package com.nowcoder.community.service;

import com.nowcoder.community.domain.entity.BusinessType;
import com.nowcoder.community.domain.entity.LikeStatus;
import com.nowcoder.community.domain.response.Result;

public interface LikeService {

    Result operate(int targetId, BusinessType businessType, LikeStatus likeStatus);

    Result getStatus(int targetId, BusinessType businessType, LikeStatus likeStatus);

    Result getLikeCount(int targetId, BusinessType businessType, LikeStatus likeStatus);

    Result getUserLikeList(BusinessType businessType);
}
