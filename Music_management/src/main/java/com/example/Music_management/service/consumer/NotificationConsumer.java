package com.example.Music_management.service.consumer;

import com.example.Music_management.entity.Notification;
import com.example.Music_management.entity.NotificationMessage;
import com.example.Music_management.entity.User;
import com.example.Music_management.exception.IncorrectNotificationException;
import com.example.Music_management.exception.UserNotExistException;
import com.example.Music_management.service.EmailService;
import com.example.Music_management.service.NotificationService;
import com.example.Music_management.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.Music_management.constant.Constant.NOTIFICATION;

/**
 * 点赞通知消费者:按消息里的 userId 查邮箱,发通知邮件并落 notification 表。
 * 失败(SMTP 不通等)抛出后由 KafkaErrorConfig 原地重试,耗尽进 NOTIFICATION.DLT 死信;
 * 消息损坏/用户不存在属于不可自愈,不重试直接进死信。
 */
@Service
public class NotificationConsumer {
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmailService emailService;

    /** 通知的发件方 = SMTP 登录账号,与 EmailServiceImpl 保持一致 */
    @Value("${spring.mail.username:}")
    private String sender;

    private final static Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener (topics = NOTIFICATION, groupId = "my-group")
    @Transactional
    public void listen(String message){
        NotificationMessage notificationMessage;
        try {
            notificationMessage = objectMapper.readValue(message, NotificationMessage.class);
        } catch (Exception e) {
            logger.error("event=NOTIFICATION_DESERIALIZE_FAILED error={}", e.getMessage(), e);
            throw new IncorrectNotificationException("bad notification message: " + e.getMessage());
        }

        User user = userService.queryById(notificationMessage.getUserId());
        if (user == null) {
            throw new UserNotExistException("notification target user not exist: " + notificationMessage.getUserId());
        }
        String email = user.getEmail();
        Notification notification = new Notification();
        notification.setDetail(notificationMessage.getMessage());
        notification.setType(notificationMessage.getType());
        notification.setSender(sender);
        notification.setReceiver(email);
        emailService.sendEmail(email,"Like Notification",notificationMessage.getMessage());
        notificationService.add(notification);
    }
}
