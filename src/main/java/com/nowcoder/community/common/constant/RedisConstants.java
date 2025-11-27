package com.nowcoder.community.common.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final String REFRESH_TOKEN_KEY = "refresh_token:";

//    // 脏评论集合,key，对应comment:like:{commentId}
//    public static final String DIRTY_COMMENT_KEY = "comment:like:dirty";
//    //  脏帖子集合,key，对应post:like:{postId}
//    public static final String DIRTY_POST_KEY = "post:like:dirty";
//    //  脏评论内容,comment:like:{commentId}
//    public static final String DIRTY_COMMENT_ID = "comment:like:";
//    //  脏帖子内容,post:like:{postId}
//    public static final String DIRTY_POST_ID = "post:like:";

//    //  评论点赞用户集合/取消赞用户集合
//    public static final String COMMENT_LIKE_USER_KEY = "comment:like:user:";
//    public static final String COMMENT_UNLIKE_USER_KEY = "comment:unlike:user:";
//    //  帖子点赞用户集合/取消赞用户集合
//    public static final String POST_LIKE_USER_KEY = "post:like:user:";
//    public static final String POST_UNLIKE_USER_KEY = "post:unlike:user:";
//
//    //  评论点赞数量
//    public static final String COMMENT_LIKE_COUNT = "comment:like:count:";
//    //  帖子点赞数量
//    public static final String POST_LIKE_COUNT = "post:like:count:";

    // Redis储存点赞/点踩数的key，形式为count:pattern:{business_id}:{message_id}，value形式为{like},{dislike}
    public static final String TARGET_LIKE_AND_DISLIKE_COUNT = "count:pattern:";
    // Redis储存用户最近点赞的key，形式为user:likes:pattern:{user_id}:{business_id}
    public static final String RECENT_USER_LIKE = "user:likes:pattern:";
    //  点赞/点踩数缓存过期时间为一小时，单位毫秒
    public static final Long TARGET_COUNT_EXPIRE = (long) (1000 * 60 * 60);
    //  用户最近点赞列表缓存过期时间为一小时，单位毫秒
    public static final Long RECENT_LIKE_EXPIRE = (long) (1000 * 60 * 60);
    //  储存脏对象列表的key，形式为dirty:likes:pattern:{business_id}
    public static final String DIRTY_LIKE_TARGET_KEY = "dirty:likes:pattern:";
}
