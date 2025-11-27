package com.nowcoder.community.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nowcoder.community.domain.po.DiscussPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscussNotice implements NoticePayload {

    public ActionType actionType;

    public DiscussPost discuss;

    public enum ActionType {
        CREATED, UPDATED, DELETED
    }
}
