package com.nowcoder.community.service;

import com.nowcoder.community.domain.dto.ReplyDTO;
import com.nowcoder.community.domain.response.Result;

public interface ReplyService {
    Result getReplySectionDetails(int discussPostId);

    Result getReplyDetails(int rpid);

    Result getReplyList(int discussPostId);

    Result addReply(int discussPostId, ReplyDTO replyDTO);

    Result deleteReply(int rpid);
}
