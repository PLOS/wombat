package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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

  private String getTypeOf(ScholarlyWorkId workId) {
    // TODO: Support with articleApi call
    return "article";
  }

  @FunctionalInterface
  private static interface TypeRedirect {
    Link get(Link.Factory factory, ScholarlyWorkId workId);
  }

  private final ImmutableMap<String, TypeRedirect> REDIRECTS = ImmutableMap.<String, TypeRedirect>builder()
      .put("article", (factory, workId) ->
          factory.toPattern(requestMappingContextDictionary, "article")
              .addQueryParameter("id", workId.getDoi())
              .build())
      .build();

  private TypeRedirect getRedirectFor(String type) {
    if (type.equals("article")) {
      return (Link.Factory factory, ScholarlyWorkId workId) ->
          factory.toPattern(requestMappingContextDictionary, "article")
              .addQueryParameter("id", workId.getDoi())
              .build();
    }
    // TODO: Others
    // TODO: Don't structure as giant if-else list
    return null;
  }

}
