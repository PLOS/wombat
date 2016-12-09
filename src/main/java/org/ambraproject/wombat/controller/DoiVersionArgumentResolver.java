package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;
import java.util.OptionalInt;

public class DoiVersionArgumentResolver implements HandlerMethodArgumentResolver {

  public static final String ID_PARAMETER = "id";
  public static final String REVISION_PARAMETER = "rev";
  public static final String INGESTION_PARAMETER = "ing";

  /**
   * Apply the policy on which users, if any, may view unpublished content.
   * <p>
   * TODO: Inject some configuration data, and/or a service for user profiles, into this resolver bean and apply it.
   *
   * @param webRequest the request from the user
   * @return {@code true} if the user is authorized to view unpublished content
   */
  private boolean canViewUnpublishedIngestion(NativeWebRequest webRequest) {
    return false;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == RequestedDoiVersion.class;
  }

  private static String resolveSingleParameter(Map<String, String[]> parameterMap, String name) {
    String[] values = parameterMap.get(name);
    if (values == null) return null;
    if (values.length > 1) throw new NotFoundException("More than one parameter with name: " + name);
    return (values.length == 0) ? null : values[0];
  }

  private static OptionalInt getNumericParameter(Map<String, String[]> parameterMap, String parameterName) {
    String param = resolveSingleParameter(parameterMap, parameterName);
    OptionalInt paramValue;
    if (param == null) {
      paramValue = OptionalInt.empty();
    } else {
      int paramNumber;
      try {
        paramNumber = Integer.parseInt(param);
      } catch (NumberFormatException e) {
        throw new NotFoundException(parameterName + " was not a number", e);
      }
      if (paramNumber < 0) throw new NotFoundException(parameterName + " must not be negative");
      paramValue = OptionalInt.of(paramNumber);
    }
    return paramValue;
  }

  @Override
  public RequestedDoiVersion resolveArgument(MethodParameter parameter,
                                             ModelAndViewContainer mavContainer,
                                             NativeWebRequest webRequest,
                                             WebDataBinderFactory binderFactory) {
    Map<String, String[]> parameterMap = webRequest.getParameterMap();

    String id = resolveSingleParameter(parameterMap, ID_PARAMETER);
    if (id == null) throw new NotFoundException("id required");

    OptionalInt revisionNumber = getNumericParameter(parameterMap, REVISION_PARAMETER);
    if (revisionNumber.isPresent()) return RequestedDoiVersion.ofRevision(id, revisionNumber.getAsInt());

    if (!revisionNumber.isPresent()) {
      OptionalInt ingestionNumber = getNumericParameter(parameterMap, INGESTION_PARAMETER);
      if (ingestionNumber.isPresent()) {
        if (canViewUnpublishedIngestion(webRequest)) {
          return RequestedDoiVersion.ofIngestion(id, ingestionNumber.getAsInt());
        } else {
          throw new NotVisibleException("Unpublished content is not visible"); // respond with 401 status instead?
        }
      }
    }

    return RequestedDoiVersion.of(id);
  }

}
