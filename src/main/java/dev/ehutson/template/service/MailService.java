package dev.ehutson.template.service;

import dev.ehutson.template.domain.UserModel;
import org.springframework.scheduling.annotation.Async;

public interface MailService {
    @Async
    void sendMail(String to, String subject, String content, boolean isMultipart, boolean isHtml);

    @Async
    void sendEmailFromTemplate(UserModel user, String templateName, String titleKey);

    @Async
    void sendActivationEmail(UserModel user);

    @Async
    void sendCreationEmail(UserModel user);

    @Async
    void sendPasswordResetMail(UserModel user);
}
