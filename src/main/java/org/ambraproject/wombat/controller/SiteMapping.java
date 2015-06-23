package org.ambraproject.wombat.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a {@link org.springframework.stereotype.Controller} class or method in order to restrict it for use with
 * only the designated {@link org.ambraproject.wombat.config.site.Site} keys
 *
 * @see
 */

@Target( {ElementType.TYPE, ElementType.METHOD } )
@Retention(RetentionPolicy.RUNTIME)
public @interface SiteMapping {

  public String[] value() default {};

  public String[] excluded() default {};

}
