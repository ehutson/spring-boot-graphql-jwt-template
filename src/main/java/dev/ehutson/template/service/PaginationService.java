package dev.ehutson.template.service;

import dev.ehutson.template.codegen.types.PaginationInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;

public interface PaginationService {
    <T> Page<T> getPage(PaginationInput paginationRequest, Function<Pageable, Page<T>> pageSupplier);

    String encodeCursor(long offset);
}
