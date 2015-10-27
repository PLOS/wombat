package org.ambraproject.wombat.config.site;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is placed on a controller's handler method that is also annotated with {@link
 * org.springframework.web.bind.annotation.RequestMapping}. It denotes that the method does not handle requests that are
 * specific to a {@link org.ambraproject.wombat.config.site.Site}, and that a site token should never be added to the
 * handler's URL pattern.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Siteless {
}
