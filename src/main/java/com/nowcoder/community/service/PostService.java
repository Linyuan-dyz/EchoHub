package com.nowcoder.community.service;

import com.nowcoder.community.domain.entity.PostDocument;
import com.nowcoder.community.domain.response.Result;

import java.util.List;

public interface PostService {

    Result search(String keyword, int page, int size);

    Result create(PostDocument postDocument);

    Result bulk(List<PostDocument> postDocuments);

    Result update(PostDocument postDocument);

    Result delete(int id);
}
