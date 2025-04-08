package dev.ehutson.template.service;

import dev.ehutson.template.dto.LocalizedMessage;
import dev.ehutson.template.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocalizedMessageTest {

    @Test
    void testConstructorWithRequiredParams() {
        // Arrange & Act
        LocalizedMessage message = new LocalizedMessage(ErrorCode.RESOURCE_NOT_FOUND, Locale.ENGLISH);

        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, message.getCode());
        assertEquals(Locale.ENGLISH, message.getLocale());
        assertNull(message.getArgs());
        assertNull(message.getContext());
    }

    @Test
    void testFullConstructor() {
        // Arrange
        Object[] args = new Object[]{"User", "123"};
        String context = "user-lookup";

        // Act
        LocalizedMessage message = new LocalizedMessage(
                ErrorCode.RESOURCE_NOT_FOUND,
                args,
                Locale.FRENCH,
                context
        );

        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, message.getCode());
        assertEquals(Locale.FRENCH, message.getLocale());
        assertArrayEquals(args, message.getArgs());
        assertEquals(context, message.getContext());
    }

    @Test
    void testWithLocale() {
        // Arrange
        LocalizedMessage original = new LocalizedMessage(
                ErrorCode.RESOURCE_NOT_FOUND,
                new Object[]{"User", "123"},
                Locale.ENGLISH,
                "context"
        );

        // Act
        LocalizedMessage modified = original.withLocale(Locale.GERMAN);

        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, modified.getCode());
        assertEquals(Locale.GERMAN, modified.getLocale());
        assertArrayEquals(original.getArgs(), modified.getArgs());
        assertEquals(original.getContext(), modified.getContext());

        // Original should be unchanged (immutability)
        assertEquals(Locale.ENGLISH, original.getLocale());
    }

    @Test
    void testWithArgs() {
        // Arrange
        Object[] originalArgs = new Object[]{"User", "123"};
        Object[] newArgs = new Object[]{"Role", "admin"};

        LocalizedMessage original = new LocalizedMessage(
                ErrorCode.RESOURCE_NOT_FOUND,
                originalArgs,
                Locale.ENGLISH,
                "context"
        );

        // Act
        LocalizedMessage modified = original.withArgs(newArgs);

        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, modified.getCode());
        assertEquals(Locale.ENGLISH, modified.getLocale());
        assertArrayEquals(newArgs, modified.getArgs());
        assertEquals(original.getContext(), modified.getContext());

        // Original should be unchanged (immutability)
        assertArrayEquals(originalArgs, original.getArgs());
    }

    @Test
    void testBuilder() {
        // Arrange
        Object[] args = new Object[]{"User", "123"};

        // Act
        LocalizedMessage message = LocalizedMessage.builder()
                .code(ErrorCode.RESOURCE_NOT_FOUND)
                .args(args)
                .locale(Locale.JAPANESE)
                .context("user-delete")
                .build();

        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, message.getCode());
        assertEquals(Locale.JAPANESE, message.getLocale());
        assertArrayEquals(args, message.getArgs());
        assertEquals("user-delete", message.getContext());
    }
}
