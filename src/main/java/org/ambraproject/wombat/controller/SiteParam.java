package org.ambraproject.wombat.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a {@link org.springframework.stereotype.Controller} method parameter that should be injected with a {@link
 * org.ambraproject.wombat.config.Site} object, to indicate which site the request belongs to.
 *
 * @see org.ambraproject.wombat.controller.SiteResolver
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SiteParam {
}
