package dev.ehutson.template.service;

import dev.ehutson.template.config.properties.ApplicationProperties;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.EmailSendFailedException;
import dev.ehutson.template.service.mail.MailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ApplicationProperties.Mail mail = new ApplicationProperties.Mail();
        mail.setFrom("from@example.com");
        mail.setBaseUrl("http://localhost");
        mail.setEnabled(true);

        lenient().when(properties.getMail()).thenReturn(mail);
    }


    @Test
    void sendMail_shouldSendEmailSuccessfully() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";
        boolean isMultipart = false;
        boolean isHtml = true;

        // Act
        mailService.sendMail(to, subject, content, isMultipart, isHtml);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendMail_shouldHandleMailException() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";
        boolean isMultipart = false;
        boolean isHtml = true;

        doThrow(new MailException("Mail sending failed") {
        }).when(mailSender).send(any(MimeMessage.class));

        // Act && Assert
        assertThrows(EmailSendFailedException.class, () -> mailService.sendMail(to, subject, content, isMultipart, isHtml));
    }

    @Test
    void sendEmailFromTemplate_shouldSendEmailSuccessfully() {
        // Arrange
        UserModel user = new UserModel();
        user.setEmail("test@example.com");
        user.setLangKey("en");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn("Test Content");
        when(messageSource.getMessage(eq(titleKey), any(), any(Locale.class))).thenReturn("Test Subject");

        // Act
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailFromTemplate_shouldHandleMissingEmail() {
        // Arrange
        UserModel user = new UserModel();
        user.setUsername("testUser");
        user.setLangKey("en");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";

        // Act
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Assert
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendActivationEmail_shouldSendActivationEmail() {
        // Arrange
        UserModel user = new UserModel();
        user.setEmail("test@example.com");
        user.setLangKey("en");

        when(templateEngine.process(eq("mail/activationEmail"), any(Context.class))).thenReturn("Test Content");
        when(messageSource.getMessage(eq("email.activation.title"), any(), any(Locale.class))).thenReturn("Activation Email");

        // Act
        mailService.sendActivationEmail(user);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendCreationEmail_shouldSendCreationEmail() {
        // Arrange
        UserModel user = new UserModel();
        user.setEmail("test@example.com");
        user.setLangKey("en");

        when(templateEngine.process(eq("mail/creationEmail"), any(Context.class))).thenReturn("Test Content");
        when(messageSource.getMessage(eq("email.activation.title"), any(), any(Locale.class))).thenReturn("Creation Email");

        // Act
        mailService.sendCreationEmail(user);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetMail_shouldSendPasswordResetEmail() {
        // Arrange
        UserModel user = new UserModel();
        user.setEmail("test@example.com");
        user.setLangKey("en");

        when(templateEngine.process(eq("mail/passwordResetEmail"), any(Context.class))).thenReturn("Test Content");
        when(messageSource.getMessage(eq("email.reset.title"), any(), any(Locale.class))).thenReturn("Password Reset Email");

        // Act
        mailService.sendPasswordResetMail(user);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }
}