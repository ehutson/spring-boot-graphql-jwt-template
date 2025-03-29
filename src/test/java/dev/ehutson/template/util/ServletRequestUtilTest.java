package dev.ehutson.template.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServletRequestUtilTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest, mockResponse);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @AfterEach
    void tearDown() throws Exception {
        RequestContextHolder.resetRequestAttributes();
        closeable.close();
    }

    @Test
    void testGetRequest_shouldReturnCurrentRequest() {
        // Act
        HttpServletRequest result = ServletRequestUtil.getRequest();
        
        // Assert
        assertNotNull(result);
        assertSame(mockRequest, result);
    }

    @Test
    void testGetResponse_shouldReturnCurrentResponse() {
        // Act
        HttpServletResponse result = ServletRequestUtil.getResponse();
        
        // Assert
        assertNotNull(result);
        assertSame(mockResponse, result);
    }

    @Test
    void testGetRequest_whenNoRequestContextExists_shouldThrowException() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();
        
        // Act & Assert
        assertThrows(IllegalStateException.class, ServletRequestUtil::getRequest);
    }

    @Test
    void testGetResponse_whenNoRequestContextExists_shouldThrowException() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();
        
        // Act & Assert
        assertThrows(IllegalStateException.class, ServletRequestUtil::getResponse);
    }
}