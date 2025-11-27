package com.nowcoder.community;

import com.nowcoder.community.common.utils.EmailUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailTest {

    @Test
    public void sendMail() {
        EmailUtils.sendMail("2109323427@qq.com", "Man", "Man, what can I say");
    }
}
