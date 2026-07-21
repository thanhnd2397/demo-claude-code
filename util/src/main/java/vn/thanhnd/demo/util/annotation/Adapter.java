package vn.thanhnd.demo.util.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stereotype annotation marking an infrastructure class as the implementation
 * of a domain-layer adapter interface (port).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Adapter {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
