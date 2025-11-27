package com.nowcoder.community.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SysNotice.class, name = "SysNotice"),
        @JsonSubTypes.Type(value = DiscussNotice.class, name = "DiscussNotice"),
        @JsonSubTypes.Type(value = LikeNotice.class, name = "LikeNotice"),
        @JsonSubTypes.Type(value = ReplyNotice.class, name = "ReplyNotice")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public interface NoticePayload {
}
