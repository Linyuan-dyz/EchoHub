package com.nowcoder.community.controller;

import com.nowcoder.community.domain.dto.ReplyDTO;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    /**
     * 查询帖子的评论区基础信息
     * @param discussPostId
     * @return
     */
    @GetMapping("/section/{discussPostId}")
    public Result getReplySectionDetails(@PathVariable int discussPostId) {
        return replyService.getReplySectionDetails(discussPostId);
    }

    /**
     * 查询某个评论的具体详情
     * @param rpid
     * @return
     */
    @GetMapping("/details/{rpid}")
    public Result getReplyDetails(@PathVariable int rpid) {
        return replyService.getReplyDetails(rpid);
    }

    /**
     * 查询帖子下的评论列表
     * @param discussPostId
     * @return
     */
    @GetMapping("/list/{discussPostId}")
    public Result getReplyList(@PathVariable int discussPostId) {
        return replyService.getReplyList(discussPostId);
    }

    @PostMapping("/add/{discussPostId}")
    public Result addReply(@PathVariable int discussPostId, @RequestBody ReplyDTO replyDTO) {
        return replyService.addReply(discussPostId, replyDTO);
    }

    @DeleteMapping("/delete/{rpid}")
    public Result deleteReply(@PathVariable int rpid) {
        return replyService.deleteReply(rpid);
    }
}
