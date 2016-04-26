package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Controller
public class ScholarlyWorkController {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @RequestMapping(name = "work", value = "/work")
  public String renderWork(HttpServletRequest request,
                           Model model,
                           @SiteParam Site site,
                           ScholarlyWorkId workId)
      throws IOException {
    return getRedirectFor(getTypeOf(workId)).get(Link.toLocalSite(site), workId).getRedirect(request);
  }

  private String getTypeOf(ScholarlyWorkId workId) throws IOException {
    Map<String, Object> workMetadata = articleApi.requestObject(workId.appendId(ApiAddress.builder("work")), Map.class);
    return (String) workMetadata.get("type");
  }

  @FunctionalInterface
  private static interface TypeRedirect {
    Link get(Link.Factory factory, ScholarlyWorkId workId);
  }

  private Link redirectFigure(Link.Factory factory, ScholarlyWorkId workId) {
    return factory.toPattern(requestMappingContextDictionary, "article/figure")
        .addQueryParameter("id", workId.getDoi())
        .build();
  }

  private final ImmutableMap<String, TypeRedirect> REDIRECTS = ImmutableMap.<String, TypeRedirect>builder()
      .put("article", (factory, workId) ->
          factory.toPattern(requestMappingContextDictionary, "article")
              .addQueryParameter("id", workId.getDoi())
              .build())
      .put("figure", this::redirectFigure)
      .put("table", this::redirectFigure)
      // TODO: supp info
      .build();

  private TypeRedirect getRedirectFor(String type) {
    TypeRedirect redirect = REDIRECTS.get(Objects.requireNonNull(type));
    if (redirect == null) {
      throw new RuntimeException("Unrecognized type: " + type);
    }
    return redirect;
  }

}
