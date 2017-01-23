/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * The values for a request mapping, with modifications taken in from application context.
 * <p/>
 * This class allows for overriding the <em>pattern</em> part of a wrapped {@link RequestMapping} object, but delegates
 * to it for all other values (such as query parameters).
 * <p/>
 * Instances are immutable. All "modifying" methods return new, modified objects.
 */
public class RequestMappingContext {

  private final RequestMapping annotation; // the raw, application-provided request mapping
  private final String pattern; // the mapping pattern, which may have been overridden by the context
  private final boolean isSiteless; // true if this object maps requests with no site resolution
  private final boolean hasSiteToken; // true if the pattern has been modified by adding a site token to the beginning

  private RequestMappingContext(RequestMapping annotation, String pattern, boolean isSiteless, boolean hasSiteToken) {
    this.annotation = Objects.requireNonNull(annotation);
    this.pattern = Objects.requireNonNull(pattern);
    this.isSiteless = isSiteless;
    this.hasSiteToken = hasSiteToken;
  }

  private static String extractPattern(RequestMapping annotation) {
    String[] valueArray = annotation.value();
    if (valueArray.length == 0) {
      String message = String.format("@RequestMapping (name=%s) must have value", annotation.name());
      throw new IllegalArgumentException(message);
    } else if (valueArray.length > 1) {
      String message = String.format("@RequestMapping (name=%s) must not have more than one value (value=%s)",
          annotation.name(), Arrays.toString(valueArray));
      throw new IllegalArgumentException(message);
    } else {
      return valueArray[0];
    }
  }

  /**
   * Wrap a raw {@link RequestMapping} object, which supplies the default mapping settings that are supplied by the
   * application.
   *
   * @param controllerMethod a handler method from a controller class
   * @return the wrapped request mapping
   */
  public static RequestMappingContext create(Method controllerMethod) {
    RequestMapping requestMapping = AnnotationUtils.findAnnotation(controllerMethod, RequestMapping.class);
    if (requestMapping == null) return null;
    boolean isSiteless = AnnotationUtils.findAnnotation(controllerMethod, Siteless.class) != null;
    return new RequestMappingContext(requestMapping, extractPattern(requestMapping), isSiteless, false);
  }

  /**
   * Create a new object that overrides this one's pattern, copying all other values.
   *
   * @param newPattern the pattern that will override the default one
   * @return a copy whose pattern is overridden
   */
  public RequestMappingContext override(String newPattern) {
    return new RequestMappingContext(annotation, newPattern, isSiteless, false);
  }

  /**
   * Create a new object that will capture a site token from the beginning of mapped URLs, copying all other values.
   *
   * @return a copy that captures a site token
   * @throws java.lang.IllegalArgumentException if this object is siteless or already captures a site token
   */
  public RequestMappingContext addSiteToken() {
    Preconditions.checkState(!isSiteless, "Cannot add site token to a siteless mapping");
    Preconditions.checkState(!hasSiteToken, "Mapping already has site token");

    String prefix = (pattern.isEmpty() || pattern.startsWith("/")) ? "/*" : "/*/";
    String modified = prefix + pattern;
    return new RequestMappingContext(annotation, modified, false, true);
  }


  /**
   * @return the raw annotation object that supplies default values for this mapping
   */
  public RequestMapping getAnnotation() {
    return annotation;
  }

  /**
   * @return the URL pattern used for this mapping
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * @return {@code false} if requests mapped by this object should apply {@link org.ambraproject.wombat.config.site.Site}
   * resolution rules; {@code true} if requests should always be mapped by this object the same way, independently of
   * sites
   * @see {@link org.ambraproject.wombat.config.site.Siteless}
   */
  public boolean isSiteless() {
    return isSiteless;
  }


  private transient ImmutableSet<String> requiredParams;
  private transient ImmutableSet<String> forbiddenParams;

  /**
   * @return all {@code param} values that the mapping requires
   */
  public Set<String> getRequiredParams() {
    if (requiredParams != null) return requiredParams;
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (String param : annotation.params()) {
      if (!param.startsWith("!")) {
        builder.add(param);
      }
    }
    return requiredParams = builder.build();
  }

  /**
   * @return all {@code param} values excluded from the mapping
   */
  public Set<String> getForbiddenParams() {
    if (forbiddenParams != null) return forbiddenParams;
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (String param : annotation.params()) {
      if (param.startsWith("!")) {
        builder.add(param.substring(1));
      }
    }
    return forbiddenParams = builder.build();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RequestMappingContext)) return false;

    RequestMappingContext that = (RequestMappingContext) o;

    if (hasSiteToken != that.hasSiteToken) return false;
    if (isSiteless != that.isSiteless) return false;
    if (!annotation.equals(that.annotation)) return false;
    if (!pattern.equals(that.pattern)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = annotation.hashCode();
    result = 31 * result + pattern.hashCode();
    result = 31 * result + (isSiteless ? 1 : 0);
    result = 31 * result + (hasSiteToken ? 1 : 0);
    return result;
  }
}
