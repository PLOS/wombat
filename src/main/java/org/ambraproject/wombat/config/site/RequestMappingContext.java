package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class RequestMappingContext {

  private final RequestMapping annotation;
  private final String pattern;
  private final boolean isSiteless;
  private final boolean hasSiteToken;

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

  public static RequestMappingContext create(Method controllerMethod) {
    RequestMapping requestMapping = AnnotationUtils.findAnnotation(controllerMethod, RequestMapping.class);
    if (requestMapping == null) return null;
    boolean isSiteless = AnnotationUtils.findAnnotation(controllerMethod, Siteless.class) != null;
    return new RequestMappingContext(requestMapping, extractPattern(requestMapping), isSiteless, false);
  }

  public RequestMappingContext override(String newPattern) {
    return new RequestMappingContext(annotation, newPattern, isSiteless, false);
  }

  public RequestMappingContext addSiteToken() {
    Preconditions.checkState(!isSiteless, "Cannot add site token to a siteless mapping");
    Preconditions.checkState(!hasSiteToken, "Mapping already has site token");

    String prefix = (pattern.isEmpty() || pattern.startsWith("/")) ? "/*" : "/*/";
    String modified = prefix + pattern;
    return new RequestMappingContext(annotation, modified, false, true);
  }


  public RequestMapping getAnnotation() {
    return annotation;
  }

  public String getPattern() {
    return pattern;
  }

  public boolean isSiteless() {
    return isSiteless;
  }


  private transient ImmutableSet<String> requiredParams;
  private transient ImmutableSet<String> forbiddenParams;

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
