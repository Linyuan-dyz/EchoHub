package com.nowcoder.community.service.Impl;

import com.nowcoder.community.domain.entity.PostDocument;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.mapper.PostRepository;
import com.nowcoder.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 根据keyword查询标题和内容，优先匹配标题，返回高亮结果
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    @Override
    public Result search(String keyword, int page, int size) {
        // 构建高亮查询
        HighlightQuery highlightQuery = new HighlightQuery(
            new Highlight(highlightParams,
                    Arrays.asList(
                    new HighlightField("title"),
                    new HighlightField("content")
            )), PostDocument.class
        );
        // 构建查询条件
        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        queryBuilder.withQuery(q -> q.bool(b -> {
            // 当关键词存在时进行标题和内容的多字段匹配搜索，标题匹配得分更高
            if (keyword != null && !keyword.isEmpty()) {
                b.must(m -> m.multiMatch(mm -> mm
                        .fields("title^3", "content")
                        .query(keyword)
                ));
            } else {
                //  当关键词不存在时进行全量查询
                b.must(m -> m.matchAll(ma -> ma));
            }
            // 暂时不做其他过滤
            return b;
        }));
        //  设置分页参数
        queryBuilder.withPageable(PageRequest.of(page, size));
        NativeQuery query = queryBuilder.build();
        SearchHits<PostDocument> hits = elasticsearchOperations.search(query, PostDocument.class);
        //  封装查询结果
        List<PostDocument> results = new ArrayList<>();
        for (SearchHit<PostDocument> hit : hits) {
            PostDocument postDocument = PostDocument.builder()
                    .id(Integer.parseInt(Objects.requireNonNull(hit.getId())))
                    .title(pickFirstHighlight(hit, "title", hit.getContent().getTitle()))
                    .content(pickFirstHighlight(hit, "content", hit.getContent().getContent()))
                    .type(hit.getContent().getType())
                    .userId(hit.getContent().getUserId())
                    .status(hit.getContent().getStatus())
                    .createTime(hit.getContent().getCreateTime())
                    .commentCount(hit.getContent().getCommentCount())
                    .score(hit.getContent().getScore())
                    .likeCount(hit.getContent().getLikeCount())
                    .build();
            results.add(postDocument);
        }
        //  封装返回内容
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("total", hits.getTotalHits());
        resp.put("page", page);
        resp.put("size", size);
        resp.put("items", results);
        return Result.ok(resp);
    }

    @Override
    public Result create(PostDocument postDocument) {
        if (postDocument.getCreateTime() == null) postDocument.setCreateTime(LocalDateTime.now());
        postRepository.save(postDocument);
        return Result.ok();
    }

    @Override
    public Result bulk(List<PostDocument> postDocuments) {
        for (PostDocument doc : postDocuments) {
            if (doc.getCreateTime() == null) doc.setCreateTime(LocalDateTime.now());
        }
        postRepository.saveAll(postDocuments);
        return Result.ok();
    }

    @Override
    public Result update(PostDocument postDocument) {
        if (postRepository.existsById(String.valueOf(postDocument.getId()))) {
            postRepository.save(postDocument);
            return Result.ok();
        } else {
            return Result.fail("文档不存在");
        }
    }

    @Override
    public Result delete(int id) {
        postRepository.deleteById(String.valueOf(id));
        return Result.ok();
    }

    HighlightParameters highlightParams = HighlightParameters.builder()
        .withPreTags("<em>")
        .withPostTags("</em>")
        .withFragmentSize(150)         // 每个片段最大长度（可按需调整）
        .withNumberOfFragments(1)      // 只要一个片段作为摘要
        // .withRequireFieldMatch(false) // 需要时可放开：允许未直接匹配字段也高亮
        // .withType(HighlightParameters.HighlightType.UNIFIED) // 如需指定高亮器类型
        .build();

    private String pickFirstHighlight(SearchHit<PostDocument> hit, String field, String defaultValue) {
        List<String> highlights = hit.getHighlightFields().get(field);
        if (highlights != null && !highlights.isEmpty()) {
            return highlights.get(0);
        }
        return defaultValue;
    }
}
