package vn.thanhnd.demo.domain.exception;

/**
 * Single validation/domain error item inside {@code ResultWrapper.errors}.
 *
 * @param field     property name that failed; {@code null} for global/object-level errors
 * @param errorCode i18n key
 * @param args      optional message arguments
 */
public record DomainError(String field, String errorCode, Object[] args) {

    public static DomainError of(String field, String code) {
        return new DomainError(field, code, null);
    }

    public static DomainError of(String field, String code, Object[] args) {
        return new DomainError(field, code, args);
    }
}
