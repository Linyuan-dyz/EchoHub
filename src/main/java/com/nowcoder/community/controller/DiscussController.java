package com.nowcoder.community.controller;

import com.nowcoder.community.domain.dto.DiscussPostDTO;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.po.DiscussPost;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.service.DiscussService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discuss")
@Slf4j
public class DiscussController {

    @Autowired
    private DiscussService discussService;

    @PostMapping("/add")
    public Result addDiscuss(@RequestBody DiscussPostDTO discussPostDTO) {
        return discussService.addDiscuss(discussPostDTO.getTitle(), discussPostDTO.getContent());
    }

    @GetMapping("/detail/{discussPostId}")
    public Result getDiscussDetail(@PathVariable int discussPostId, PageDTO pageDTO) {
        return discussService.getDiscussById(discussPostId, pageDTO);
    }

    @PostMapping("/update")
    public Result updateDiscuss(@RequestBody DiscussPost discussPost) {
        return discussService.updateDiscuss(discussPost);
    }

    @DeleteMapping("/{discussPostId}")
    public Result deleteDiscuss(@PathVariable int discussPostId) {
        return discussService.deleteDiscuss(discussPostId);
    }

}
