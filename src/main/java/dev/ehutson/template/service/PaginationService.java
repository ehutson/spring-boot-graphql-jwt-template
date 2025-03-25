package dev.ehutson.template.service;

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
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public <T> Page<T> getPage(String cursor, Integer first, Integer last, Function<Pageable, Page<T>> pageSupplier) {
        int limit = determineLimit(first, last);

        // Decode cursor if present
        Pageable pageable;
        if (cursor != null && !cursor.isEmpty()) {
            long offset = decodeCursor(cursor);
            pageable = PageRequest.of((int) (offset / limit), limit);
        } else {
            pageable = PageRequest.of(0, limit);
        }

        return pageSupplier.apply(pageable);
    }

    public String encodeCursor(long offset) {
        return Base64.getEncoder().encodeToString(String.valueOf(offset).getBytes());
    }

    private long decodeCursor(String cursor) {
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Long.parseLong(decoded);
        } catch (Exception e) {
            return 0L;
        }
    }

    private int determineLimit(Integer first, Integer last) {
        int limit;
        if (first != null && first > 0) {
            limit = Math.min(first, MAX_PAGE_SIZE);
        } else if (last != null && last > 0) {
            limit = Math.min(last, MAX_PAGE_SIZE);
        } else {
            limit = DEFAULT_PAGE_SIZE;
        }
        return limit;
    }
}
