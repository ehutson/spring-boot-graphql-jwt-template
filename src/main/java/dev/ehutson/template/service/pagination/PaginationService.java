package dev.ehutson.template.service.pagination;

import dev.ehutson.template.codegen.types.PaginationInput;
import dev.ehutson.template.config.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaginationService {

    private final ApplicationProperties properties;

    private static String getCursorFromAfterOrBeforeValues(PaginationInput paginationInput) {
        return paginationInput.getAfter() != null ? paginationInput.getAfter() : paginationInput.getBefore();
    }

    /**
     * Returns the page of data based on pagination parameters.
     *
     * @param paginationInput Contains all pagination parameters
     * @param pageSupplier    Function to retrieve the actual page data
     * @param <T>             Type of the data being paginated
     * @return Page of data
     */
    public <T> Page<T> getPage(PaginationInput paginationInput, Function<Pageable, Page<T>> pageSupplier) {
        // Calculate the limit and offset
        int limit = determineLimit(paginationInput.getFirst(), paginationInput.getLast());
        long offset = getOffsetFromEncodedCursor(getCursorFromAfterOrBeforeValues(paginationInput));
        int pageNumber = calculatePageNumber(offset, limit);

        // Create pageable object
        Pageable pageable = PageRequest.of(pageNumber, limit);

        // Apply the page supplier function with our calculated pageable
        return pageSupplier.apply(pageable);
    }

    /**
     * Converts an offset to a Base64 Cursor.
     *
     * @param offset The offset to encode
     * @return Base64 encoded cursor
     */
    public String encodeCursor(long offset) {
        return Base64.getEncoder().encodeToString(String.valueOf(offset).getBytes());
    }

    /**
     * Extracts the offset from a cursor
     *
     * @param cursor The cursor to decode
     * @return The extracted offset, or 0 if invalid
     */
    private long getOffsetFromEncodedCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return 0L;
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Long.parseLong(decoded);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Calculates the page number from offset and limit.
     *
     * @param offset The offset in the dataset
     * @param limit  Items per page
     * @return Page number (0-based)
     */
    private int calculatePageNumber(long offset, int limit) {
        return limit > 0 ? (int) (offset / limit) : 0;
    }

    /**
     * Determines the limit based on parameters
     *
     * @param first First n items parameter
     * @param last  Last n items parameter
     * @return The calculated limit value
     */
    private int determineLimit(Integer first, Integer last) {
        if (first != null && first > 0) {
            return Math.min(first, properties.getPagination().getMaxPageSize());
        }
        if (last != null && last > 0) {
            return Math.min(last, properties.getPagination().getMaxPageSize());
        }
        return properties.getPagination().getPageSize();
    }
}
