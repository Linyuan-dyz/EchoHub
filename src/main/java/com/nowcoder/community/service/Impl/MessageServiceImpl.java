package com.nowcoder.community.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.common.utils.UserInfoHolder;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.entity.SysNotice;
import com.nowcoder.community.domain.po.Message;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.domain.vo.LetterListVO;
import com.nowcoder.community.domain.vo.LetterVO;
import com.nowcoder.community.domain.vo.PosterVO;
import com.nowcoder.community.mapper.MessageMapper;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nowcoder.community.domain.entity.SysNotice.ObjectType.POST;

@Service
public class MessageServiceImpl implements MessageService {

    private static final String CONVERSATION_FORM = "\\d+_\\d+";
    private static final int SYSTEM_USER_ID = 1;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result getLetterList(PageDTO pageDTO) {
        return getMessageList(pageDTO, true);
    }

    private Result getMessageList(PageDTO pageDTO, boolean isConversation) {
        List<LetterVO> list = new ArrayList<>();
        if (UserInfoHolder.getUser() == null) {
            return Result.fail("用户未登录");
        }
        Page<Map<String, Object>> page = new Page<>(pageDTO.getCurrent(), pageDTO.getLimit());
        //  获取用户会话中的最新一条私信
        List<Message> messages = getLatestMessage(page, CONVERSATION_FORM, isConversation);
        if (messages == null) {
            return Result.fail("用户未登录");
        }
        //  将消息进行封装
        for (Message message : messages) {
            int posterId = message.getFromId() == UserInfoHolder.getUser().getId() ? message.getToId() : message.getFromId();
            LetterVO letterVO = LetterVO.builder()
                    .message(message)
                    .poster(getPosterVO(posterId))
                    .build();
            list.add(letterVO);
        }
        LetterListVO letterList = LetterListVO.builder()
                .total((long) messages.size())
                .letters(list)
                .build();
        return Result.ok(letterList);
    }

    @Override
    public Result getLetterDetail(String conversationId) {
        if (UserInfoHolder.getUser() == null) {
            return Result.fail("用户未登录");
        }
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId);
        List<Message> messages = messageMapper.selectList(wrapper);
        List<LetterVO> letters = new ArrayList<>();
        List<Integer> letterIds = new ArrayList<>();
        for (Message message : messages) {
            int letterId = message.getId();
            //  找出当前对话中所有当前用户未读的私信
            if (message.getToId() == UserInfoHolder.getUser().getId() &&
                    message.getStatus() == Message.MESSAGE_UNREAD) {
                letterIds.add(letterId);
            }
            letterIds.add(letterId);
            int posterId = message.getFromId();
            PosterVO posterVO = getPosterVO(posterId);
            LetterVO letterVO = LetterVO.builder()
                    .message(message)
                    .poster(posterVO)
                    .build();
            letters.add(letterVO);
        }
        //  TODO:异步将未读消息标记为已读
        if (!letterIds.isEmpty()) {
            messageMapper.update(new UpdateWrapper<Message>()
                    .set("status", Message.MESSAGE_READ)
                    .in("id", letterIds));
        }
        Long total = (long) letters.size();
        LetterListVO letterListVO = LetterListVO.builder()
                .total(total)
                .letters(letters)
                .build();
        return Result.ok(letterListVO);
    }

    @Transactional
    @Override
    public Result sendLetter(String toName, String content) {
        User toUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", toName));
        if (toUser == null) {
            return Result.fail("目标用户不存在");
        }
        if (UserInfoHolder.getUser() == null) {
            return Result.fail("用户未登录");
        }
        User fromUser = UserInfoHolder.getUser();
        Message message = Message.builder()
                .fromId(fromUser.getId())
                .toId(toUser.getId())
                .status(Message.MESSAGE_UNREAD)
                .content(content)
                .conversationId(fromUser.getId() < toUser.getId() ?
                        String.format("%d_%d", fromUser.getId(), toUser.getId()) :
                        String.format("%d_%d", toUser.getId(), fromUser.getId()))
                .build();
        messageMapper.insert(message);
        //  TODO:后续实现消息异步通知接收方
        return Result.ok();
    }

    @Override
    public Result getNoticeList(PageDTO pageDTO) {
        return getMessageList(pageDTO, false);
    }

    @Override
    public Result getNoticeDetail(String topic, PageDTO pageDTO) {
        return Result.fail("功能未完成");
    }

    //  获取私信的对话方信息
    private PosterVO getPosterVO(int posterId) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", posterId));
        return BeanUtil.copyProperties(user, PosterVO.class);
    }

    /**
     * 获取当前用户的最新私信或通知
     * @return
     */
    private List<Message> getLatestMessage(Page<Map<String, Object>> page,
                                           String conversationIdForm, boolean isConversation) {
        if (UserInfoHolder.getUser() == null) {
            return null;
        }
        int userId = UserInfoHolder.getUser().getId();
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.select("conversation_id", "MAX(id) as lastId")
                .eq("from_id", userId).or().eq("to_id", userId)
                .groupBy("conversation_id");
        Page<Map<String, Object>> mapPage = messageMapper.selectMapsPage(page, wrapper);
        List<Integer> lastIds = mapPage.getRecords().stream()
                .filter(m -> {
                    boolean isConversationId = m.get("conversation_id").toString().matches(conversationIdForm);
                    return isConversation == isConversationId;
                })
                .map(m -> (int) m.get("lastId"))
                .collect(Collectors.toList());
        if (lastIds.isEmpty()) {
            return new ArrayList<>();
        }
        return messageMapper.selectBatchIds(lastIds);
    }

    public void saveSystemMessage(EventNotice eventNotice) {
        NoticePayload noticePayload = eventNotice.getExtra();
        //  调用方已经确认消息类型
        SysNotice sysNotice = (SysNotice) noticePayload;
        StringBuilder content = new StringBuilder();
        StringBuilder conversationId = new StringBuilder();

        switch (sysNotice.getEventType()) {
            case LIKE -> {
                conversationId.append("like");
                content.append(sysNotice.getActorUserId()).append("点赞了你的");
            }
            case COMMENT -> {
                conversationId.append("comment");
                content.append(sysNotice.getActorUserId()).append("评论了你的");
            }
        }
        switch (sysNotice.getObjectType()) {
            case POST -> content.append("帖子");
            case COMMENT -> content.append("评论");
        }
        Message message = Message.builder()
                .fromId(SYSTEM_USER_ID)
                .toId(sysNotice.getTargetUserId())
                .status(Message.MESSAGE_UNREAD)
                .content(content.toString())
                .conversationId(conversationId.toString())
                .build();
        messageMapper.insert(message);
    }
}
