package dev.ehutson.template.service;

import dev.ehutson.template.config.TestContainersConfig;
import dev.ehutson.template.config.properties.ApplicationProperties;
import dev.ehutson.template.domain.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    GenericContainer<?> mailhogContainer;

    private MailhogClient mailhogClient;

    @BeforeEach
    void setUp() {
        mailhogClient = new MailhogClient(getMailhogUiUrl());
        // Clean emails before each test
        mailhogClient.deleteAllEmails();

        // Ensure mail is enabled for tests
        applicationProperties.getMail().setEnabled(true);
        applicationProperties.getMail().setFrom("test@example.com");
        applicationProperties.getMail().setBaseUrl("http://localhost:8080");
    }

    private String getMailhogUiUrl() {
        return String.format("http://%s:%d", mailhogContainer.getHost(),
                mailhogContainer.getMappedPort(8025));
    }

    @Test
    void testActivationEmail() {
        // Arrange
        UserModel user = createTestUser();
        user.setActivationKey(UUID.randomUUID().toString());

        // Act
        mailService.sendActivationEmail(user);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<MailhogClient.Email> emailOpt = mailhogClient.findEmailToRecipient(user.getEmail());

            assertThat(emailOpt).isPresent();
            MailhogClient.Email email = emailOpt.get();
            assertThat(email.getSubject()).isEqualTo("Activate your account");
            assertThat(email.getFrom()).isEqualTo(applicationProperties.getMail().getFrom());
            assertThat(email.getBody()).contains("Hello, Booboo");
            assertThat(email.getBody()).contains("To activate your account");
            assertThat(email.getBody()).contains(user.getActivationKey());
        });
    }

    @Test
    void testCreationEmail() {
        // Arrange
        UserModel user = createTestUser();
        user.setResetKey(UUID.randomUUID().toString());

        // Act
        mailService.sendCreationEmail(user);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<MailhogClient.Email> emailOpt = mailhogClient.findEmailToRecipient(user.getEmail());

            assertThat(emailOpt).isPresent();
            MailhogClient.Email email = emailOpt.get();
            assertThat(email.getSubject()).contains("Activate your account");
            assertThat(email.getFrom()).isEqualTo(applicationProperties.getMail().getFrom());
            assertThat(email.getBody()).contains("Hello, Booboo");
            assertThat(email.getBody()).contains("Your account has been created. Please click on the following link to access it");
            assertThat(email.getBody()).contains(user.getResetKey());
        });
    }

    @Test
    void testPasswordResetEmail() {
        // Arrange
        UserModel user = createTestUser();
        user.setResetKey(UUID.randomUUID().toString());

        // Act
        mailService.sendPasswordResetMail(user);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<MailhogClient.Email> emailOpt = mailhogClient.findEmailToRecipient(user.getEmail());

            assertThat(emailOpt).isPresent();
            MailhogClient.Email email = emailOpt.get();
            assertThat(email.getSubject()).contains("Reset your password");
            assertThat(email.getFrom()).isEqualTo(applicationProperties.getMail().getFrom());
            assertThat(email.getBody()).contains("Hello, Booboo");
            assertThat(email.getBody()).contains("A password reset has been requested for your account. Please click on the following link to reset your password:");
            assertThat(email.getBody()).contains(user.getResetKey());
        });
    }

    private UserModel createTestUser() {
        UserModel user = new UserModel();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setLangKey("en");
        user.setFirstName("Booboo");
        user.setLastName("Hutsnuffer");
        return user;
    }
}
