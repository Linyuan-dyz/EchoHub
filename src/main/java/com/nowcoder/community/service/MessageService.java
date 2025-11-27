package com.nowcoder.community.service;

import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.response.Result;

public interface MessageService {
    Result getLetterList(PageDTO pageDTO);

    Result getLetterDetail(String conversationId);

    Result sendLetter(String toName, String content);

    Result getNoticeList(PageDTO pageDTO);

    Result getNoticeDetail(String topic, PageDTO pageDTO);

    void saveSystemMessage(EventNotice sysNotice);
}
