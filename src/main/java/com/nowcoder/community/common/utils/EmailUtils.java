package com.nowcoder.community.common.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.util.Properties;

@Slf4j
public class EmailUtils {

    private static final TemplateEngine templateEngine;


    static {
        templateEngine = new TemplateEngine();
    }

    public static void sendMail(String to, String subject, String username) {
        try {
            JavaMailSender mailSender = getJavaMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            Context context = new Context();
            context.setVariable("Username", username);
            helper.setFrom("1920ww10@sina.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(username, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("邮件发送失败：{}", e.getMessage());
        }
    }

    private static JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.sina.com");
        mailSender.setPort(587);

        mailSender.setUsername("1920ww10@sina.com");
        mailSender.setPassword("8b41bf3003701db4");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
