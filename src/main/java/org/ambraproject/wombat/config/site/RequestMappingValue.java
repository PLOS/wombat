package org.ambraproject.wombat.config.site;

import com.google.common.collect.ImmutableSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class RequestMappingValue {

  private final RequestMapping annotation;
  private final String pattern;
  private final boolean isSiteless;
  private final boolean hasSiteToken;

  private RequestMappingValue(RequestMapping annotation, String pattern, boolean isSiteless, boolean hasSiteToken) {
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


  // Before you refactor the two methods below, they have less duplication than it looks like.
  // Note that they are calling two different signatures of AnnotationUtils.findAnnotation.

  public static RequestMappingValue create(Method controllerMethod) {
    RequestMapping requestMapping = AnnotationUtils.findAnnotation(controllerMethod, RequestMapping.class);
    if (requestMapping == null) return null;
    boolean isSiteless = AnnotationUtils.findAnnotation(controllerMethod, Siteless.class) != null;
    return create(requestMapping, isSiteless);
  }

  public static RequestMappingValue create(Class<?> controllerClass) {
    RequestMapping requestMapping = AnnotationUtils.findAnnotation(controllerClass, RequestMapping.class);
    if (requestMapping == null) return null;
    boolean isSiteless = AnnotationUtils.findAnnotation(controllerClass, Siteless.class) != null;
    return create(requestMapping, isSiteless);
  }

  private static RequestMappingValue create(RequestMapping requestMapping, boolean isSiteless) {
    return new RequestMappingValue(requestMapping, extractPattern(requestMapping), isSiteless, false);
  }


  public RequestMappingValue override(String newPattern) {
    return new RequestMappingValue(annotation, newPattern, isSiteless, false);
  }

  public RequestMappingValue addSiteToken() {
    if (isSiteless) throw new IllegalStateException("Cannot add site token to a siteless mapping");
    if (hasSiteToken) throw new IllegalStateException("Mapping already has site token");

    StringBuilder modified = new StringBuilder(pattern.length() + 3);
    modified.append("/*");
    if (!pattern.isEmpty() && !pattern.startsWith("/")) {
      modified.append('/');
    }
    modified.append(pattern);
    return new RequestMappingValue(annotation, modified.toString(), false, true);
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
      if (!param.startsWith("!")) {
        builder.add(param.substring(1));
      }
    }
    return forbiddenParams = builder.build();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RequestMappingValue)) return false;

    RequestMappingValue that = (RequestMappingValue) o;

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
