package org.ambraproject.wombat.config.site;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the set of {@link SiteScope} values that apply to a request handler.
 * <p>
 * A non-empty set of {@link SiteScope} values should be provided on every method that is annotated with {@link
 * org.springframework.web.bind.annotation.RequestMapping}.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingSiteScope {

  SiteScope[] value();

}
