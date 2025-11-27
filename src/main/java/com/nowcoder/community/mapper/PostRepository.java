package com.nowcoder.community.mapper;

import com.nowcoder.community.domain.entity.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends ElasticsearchRepository<PostDocument, String> {

}
