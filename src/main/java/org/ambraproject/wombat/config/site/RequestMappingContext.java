package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
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

  /**
   * Application-defined annotation types that can be applied to a request-mapped handler method.
   */
  private static enum CustomAnnotation {
    SITELESS(Siteless.class), JOURNAL_NEUTRAL(JournalNeutral.class);

    private final Class<? extends Annotation> type;

    private CustomAnnotation(Class<? extends Annotation> type) {
      this.type = type;
    }

    private static Set<CustomAnnotation> findOn(Method method) {
      Set<CustomAnnotation> annotationReprs = EnumSet.allOf(CustomAnnotation.class);
      for (Iterator<CustomAnnotation> iterator = annotationReprs.iterator(); iterator.hasNext(); ) {
        if (AnnotationUtils.findAnnotation(method, iterator.next().type) == null) {
          iterator.remove();
        }
      }
      return annotationReprs;
    }
  }

  private final RequestMapping mapping; // the raw, application-provided request mapping
  private final String pattern; // the mapping pattern, which may have been overridden by the context
  private final boolean hasSiteToken; // true if the pattern has been modified by adding a site token to the beginning
  private final ImmutableSet<CustomAnnotation> annotations; // annotations other than RequestMapping on the handler

  private RequestMappingContext(RequestMapping mapping, String pattern, boolean hasSiteToken, Set<CustomAnnotation> annotations) {
    this.mapping = Objects.requireNonNull(mapping);
    this.pattern = Objects.requireNonNull(pattern);
    this.annotations = Sets.immutableEnumSet(annotations);
    this.hasSiteToken = hasSiteToken;
  }

  private static String extractPattern(RequestMapping mapping) {
    String[] valueArray = mapping.value();
    if (valueArray.length == 0) {
      String message = String.format("@RequestMapping (name=%s) must have value", mapping.name());
      throw new IllegalArgumentException(message);
    } else if (valueArray.length > 1) {
      String message = String.format("@RequestMapping (name=%s) must not have more than one value (value=%s)",
          mapping.name(), Arrays.toString(valueArray));
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
    String pattern = extractPattern(requestMapping);
    Set<CustomAnnotation> customAnnotations = CustomAnnotation.findOn(controllerMethod);
    return new RequestMappingContext(requestMapping, pattern, false, customAnnotations);
  }

  /**
   * Create a new object that overrides this one's pattern, copying all other values.
   *
   * @param newPattern the pattern that will override the default one
   * @return a copy whose pattern is overridden
   */
  public RequestMappingContext override(String newPattern) {
    return new RequestMappingContext(mapping, newPattern, false, annotations);
  }

  /**
   * Create a new object that will capture a site token from the beginning of mapped URLs, copying all other values.
   *
   * @return a copy that captures a site token
   * @throws java.lang.IllegalArgumentException if this object is siteless or already captures a site token
   */
  public RequestMappingContext addSiteToken() {
    Preconditions.checkState(!isSiteless(), "Cannot add site token to a siteless mapping");
    Preconditions.checkState(!hasSiteToken, "Mapping already has site token");

    String prefix = (pattern.isEmpty() || pattern.startsWith("/")) ? "/*" : "/*/";
    String modified = prefix + pattern;
    return new RequestMappingContext(mapping, modified, true, annotations);
  }


  /**
   * @return the raw mapping object that supplies default values for this mapping
   */
  public RequestMapping getAnnotation() {
    return mapping;
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
    return annotations.contains(CustomAnnotation.SITELESS);
  }


  private transient ImmutableSet<String> requiredParams;
  private transient ImmutableSet<String> forbiddenParams;

  /**
   * @return all {@code param} values that the mapping requires
   */
  public Set<String> getRequiredParams() {
    if (requiredParams != null) return requiredParams;
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (String param : mapping.params()) {
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
    for (String param : mapping.params()) {
      if (param.startsWith("!")) {
        builder.add(param.substring(1));
      }
    }
    return forbiddenParams = builder.build();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RequestMappingContext that = (RequestMappingContext) o;

    if (hasSiteToken != that.hasSiteToken) return false;
    if (!mapping.equals(that.mapping)) return false;
    if (!pattern.equals(that.pattern)) return false;
    return annotations.equals(that.annotations);

  }

  @Override
  public int hashCode() {
    int result = mapping.hashCode();
    result = 31 * result + pattern.hashCode();
    result = 31 * result + (hasSiteToken ? 1 : 0);
    result = 31 * result + annotations.hashCode();
    return result;
  }
}
