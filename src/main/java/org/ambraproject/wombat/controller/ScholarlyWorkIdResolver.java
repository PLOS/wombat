package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;
import java.util.OptionalInt;

public class ScholarlyWorkIdResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == ScholarlyWorkId.class;
  }

  private String resolveSingleParameter(Map<String, String[]> parameterMap, String name) {
    String[] values = parameterMap.get(name);
    if (values == null) return null;
    if (values.length > 1) throw new NotFoundException("More than one parameter with name: " + name);
    return (values.length == 0) ? null : values[0];
  }

  @Override
  public ScholarlyWorkId resolveArgument(MethodParameter parameter,
                                         ModelAndViewContainer mavContainer,
                                         NativeWebRequest webRequest,
                                         WebDataBinderFactory binderFactory) {
    Map<String, String[]> parameterMap = webRequest.getParameterMap();

    String id = resolveSingleParameter(parameterMap, "id");
    if (id == null) throw new NotFoundException("id required");

    String rev = resolveSingleParameter(parameterMap, "rev");
    OptionalInt revisionNumber;
    if (rev == null) {
      revisionNumber = OptionalInt.empty();
    } else {
      int revisionNumberValue;
      try {
        revisionNumberValue = Integer.parseInt(rev);
      } catch (NumberFormatException e) {
        throw new NotFoundException("rev was not a number", e);
      }
      if (revisionNumberValue < 0) throw new NotFoundException("rev must not be negative");
      revisionNumber = OptionalInt.of(revisionNumberValue);
    }

    return new ScholarlyWorkId(id, revisionNumber);
  }

}
