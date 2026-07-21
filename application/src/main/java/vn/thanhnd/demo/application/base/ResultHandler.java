package vn.thanhnd.demo.application.base;

import vn.thanhnd.demo.domain.exception.DomainError;
import vn.thanhnd.demo.domain.exception.DomainValidationException;

import java.util.List;
import java.util.function.Supplier;

/**
 * Runs a command use case's core logic and converts a {@link DomainValidationException} into a
 * failed {@link ResultWrapper}. Any other exception propagates to presentation-layer global handlers.
 */
public final class ResultHandler {

    private ResultHandler() {
    }

    public static <T> ResultWrapper<T> handle(Supplier<T> block) {
        try {
            return ResultWrapper.success(block.get());
        } catch (DomainValidationException e) {
            DomainError error = DomainError.of(null, e.getMessage());
            return ResultWrapper.failure(List.of(error));
        }
    }
}
