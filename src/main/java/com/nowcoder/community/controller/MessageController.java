package com.nowcoder.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/letter/list")
    public Result getLetterList(PageDTO pageDTO) {
        return messageService.getLetterList(pageDTO);
    }

    @GetMapping("/letter/detail/{conversationId}")
    public Result getLetterDetail(@PathVariable String conversationId) {
        return messageService.getLetterDetail(conversationId);
    }

    @PostMapping("/letter/send")
    public Result sendLetter(@RequestParam String toName, @RequestParam String content) {
        return messageService.sendLetter(toName, content);
    }

    @GetMapping("/notice/list")
    public Result getNoticeList(PageDTO pageDTO) {
        return messageService.getNoticeList(pageDTO);
    }

    //TODO:功能尚未完成
    @GetMapping("/notice/detail/{topic}")
    public Result getNoticeDetail(@PathVariable String topic, PageDTO pageDTO) {
        return messageService.getNoticeDetail(topic, pageDTO);
    }
}
