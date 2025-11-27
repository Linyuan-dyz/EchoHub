package com.nowcoder.community.controller;

import com.nowcoder.community.domain.entity.PostDocument;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    public PostService postService;

    @GetMapping("/search")
    public Result search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        return postService.search(keyword, page, size);
    }

    @PostMapping("/create")
    public Result create(@RequestBody PostDocument postDocument) {
        return postService.create(postDocument);
    }

    @PostMapping("/bulk")
    public Result bulk(@RequestBody List<PostDocument> postDocuments) {
        return postService.bulk(postDocuments);
    }

    @PostMapping("/update")
    public Result update(@RequestBody PostDocument postDocument) {
        return postService.update(postDocument);
    }

    @DeleteMapping("/{id}")
    public Result delete(@RequestParam int id) {
        return postService.delete(id);
    }
}
