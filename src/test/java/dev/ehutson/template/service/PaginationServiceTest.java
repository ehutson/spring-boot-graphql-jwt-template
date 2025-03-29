package dev.ehutson.template.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaginationServiceTest {

    @InjectMocks
    private PaginationService paginationService;

    @Mock
    private Function<Pageable, Page<String>> pageSupplier;

    @Test
    void testEncodeCursor() {
        // Test with offset 0
        String encoded0 = paginationService.encodeCursor(0);
        assertEquals(Base64.getEncoder().encodeToString("0".getBytes()), encoded0);

        // Test with offset 10
        String encoded10 = paginationService.encodeCursor(10);
        assertEquals(Base64.getEncoder().encodeToString("10".getBytes()), encoded10);

        // Test with large offset
        String encodedLarge = paginationService.encodeCursor(1000000);
        assertEquals(Base64.getEncoder().encodeToString("1000000".getBytes()), encodedLarge);
    }

    @Test
    void testGetPage_withNullCursor() {
        // Arrange
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 20), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(null, null, null, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        verify(pageSupplier).apply(PageRequest.of(0, 20));
    }

    @Test
    void testGetPage_withEmptyCursor() {
        // Arrange
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 20), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage("", null, null, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        verify(pageSupplier).apply(PageRequest.of(0, 20));
    }

    @Test
    void testGetPage_withValidCursor() {
        // Arrange
        String cursor = Base64.getEncoder().encodeToString("40".getBytes());
        List<String> data = Arrays.asList("Item41", "Item42", "Item43");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(2, 20), 43);

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(cursor, null, null, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        verify(pageSupplier).apply(PageRequest.of(2, 20));
    }

    @Test
    void testGetPage_withInvalidCursor() {
        // Arrange
        // Not a valid Base64 string
        String cursor = "not-a-valid-cursor";
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 20), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(cursor, null, null, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        // Should default to page 0 when cursor is invalid
        verify(pageSupplier).apply(PageRequest.of(0, 20));
    }

    @Test
    void testGetPage_withNonIntegerCursor() {
        // Arrange
        // Valid Base64 but not a valid number
        String cursor = Base64.getEncoder().encodeToString("not-a-number".getBytes());
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 20), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(cursor, null, null, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        // Should default to page 0 when cursor contains invalid number
        verify(pageSupplier).apply(PageRequest.of(0, 20));
    }

    @Test
    void testGetPage_withCustomFirstLimit() {
        // Arrange
        List<String> data = Arrays.asList("Item1", "Item2", "Item3", "Item4", "Item5");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 5), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(null, 5, null, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        verify(pageSupplier).apply(PageRequest.of(0, 5));
    }

    @Test
    void testGetPage_withCustomLastLimit() {
        // Arrange
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 3), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(null, null, 3, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        verify(pageSupplier).apply(PageRequest.of(0, 3));
    }

    @Test
    void testGetPage_withFirstExceedingMaxLimit() {
        // Arrange
        // MAX_PAGE_SIZE is 100
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 100), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(null, 150, null, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        verify(pageSupplier).apply(PageRequest.of(0, 100));
    }

    @Test
    void testGetPage_withBothFirstAndLastProvided() {
        // Arrange
        // First should take precedence over last
        List<String> data = Arrays.asList("Item1", "Item2", "Item3", "Item4", "Item5");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, 5), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        Page<String> result = paginationService.getPage(null, 5, 10, pageSupplier);

        // Assert
        assertEquals(expectedPage, result);
        verify(pageSupplier).apply(PageRequest.of(0, 5));
    }

    @ParameterizedTest
    @MethodSource("limitProvider")
    void testDetermineLimit(Integer first, Integer last, int expectedLimit) {
        // This test uses reflection to test the private method

        // Arrange
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> expectedPage = new PageImpl<>(data, PageRequest.of(0, expectedLimit), data.size());

        when(pageSupplier.apply(any())).thenReturn(expectedPage);

        // Act
        paginationService.getPage(null, first, last, pageSupplier);

        // Assert
        verify(pageSupplier).apply(PageRequest.of(0, expectedLimit));
    }

    private static Stream<Arguments> limitProvider() {
        return Stream.of(
                // first, last, expected limit
                Arguments.of(10, null, 10),        // first specified
                Arguments.of(null, 15, 15),        // last specified
                Arguments.of(10, 15, 10),          // both specified, first takes precedence
                Arguments.of(150, null, 100),      // first exceeds max
                Arguments.of(null, 200, 100),      // last exceeds max
                Arguments.of(-5, null, 20),        // invalid first, use default
                Arguments.of(null, -10, 20),       // invalid last, use default
                Arguments.of(null, null, 20)       // none specified, use default
        );
    }
}