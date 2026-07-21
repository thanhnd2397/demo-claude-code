package vn.thanhnd.demo.util.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stereotype annotation marking a class as an application-layer use case.
 * One use case = one business operation, exposed via a single public method.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface UseCase {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
