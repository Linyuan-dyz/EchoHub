package com.nowcoder.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.domain.dto.DiscussPostDTO;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.po.DiscussPost;
import com.nowcoder.community.domain.response.Result;

public interface DiscussService extends IService<DiscussPost> {
    Result addDiscuss(String title, String content);

    Result getDiscussById(int discussId, PageDTO pageDTO);

    Result updateDiscuss(DiscussPost discussPost);

    Result deleteDiscuss(int discussId);
}
