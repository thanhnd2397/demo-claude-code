package vn.thanhnd.demo.domain.cache;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface for domain objects that can be stored inside a {@link Cache} envelope.
 * The JSON discriminator property is {@code "dataType"} (not {@code "type"}).
 *
 * <p>To make a domain object cacheable: implement this interface, then either annotate the
 * class with {@code @JsonTypeName("uniqueName")} or add a {@link JsonSubTypes.Type} entry below.
 * The registry starts empty — no cacheable domain object exists yet in this skeleton.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "dataType")
@JsonSubTypes({
})
public interface CacheData {
}
