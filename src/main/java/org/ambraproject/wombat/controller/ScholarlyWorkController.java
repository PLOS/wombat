package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Controller
public class ScholarlyWorkController {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @RequestMapping(name = "work", value = "/work")
  public String redirectToWork(HttpServletRequest request,
                               @SiteParam Site site,
                               ScholarlyWorkId workId)
      throws IOException {
    return getRedirectFor(site, workId).getRedirect(request);
  }

  private Map<String, Object> getWorkMetadata(ScholarlyWorkId workId) throws IOException {
    ApiAddress address = workId.appendId(ApiAddress.builder("work"));
    try {
      return articleApi.requestObject(address, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException("No work exists with ID: " + workId, e);
    }
  }

  private String getTypeOf(ScholarlyWorkId workId) throws IOException {
    return (String) getWorkMetadata(workId).get("type");
  }

  private static final ImmutableMap<String, String> REDIRECT_HANDLERS = ImmutableMap.<String, String>builder()
      .put("article", "article")
      .put("figure", "figurePage")
      .put("table", "figurePage")
          // TODO: supp info
      .build();

  private Link getRedirectFor(Site site, ScholarlyWorkId workId) throws IOException {
    String handlerName = REDIRECT_HANDLERS.get(getTypeOf(workId));
    if (handlerName == null) {
      throw new RuntimeException("Unrecognized type: " + workId);
    }
    Link.Factory.PatternBuilder handlerLink = Link.toLocalSite(site)
        .toPattern(requestMappingContextDictionary, handlerName);
    return pointLinkToWork(handlerLink, workId);
  }

  private static Link pointLinkToWork(Link.Factory.PatternBuilder link, ScholarlyWorkId workId) {
    link.addQueryParameter("id", workId.getDoi());
    workId.getRevisionNumber().ifPresent(revisionNumber ->
        link.addQueryParameter("rev", revisionNumber));
    return link.build();
  }

  @RequestMapping(name = "workFile", value = "/work", params = {"fileType"})
  public void redirectToWorkFile(HttpServletRequest request,
                                 @SiteParam Site site,
                                 ScholarlyWorkId workId,
                                 @RequestParam("fileType") String fileType)
      throws IOException {
    // TODO
  }

}
