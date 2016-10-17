package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Mappings for requests for DOIs that belong to works of unknown type.
 */
@Controller
public class GeneralDoiController extends WombatController {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @RequestMapping(name = "doi", value = "/doi")
  public RedirectView redirectFromDoi(HttpServletRequest request,
                                      @SiteParam Site site,
                                      RequestedDoiVersion id)
      throws IOException {
    return getRedirectFor(site, id).getRedirect(request);
  }

  Map<String, Object> getMetadataForDoi(RequestedDoiVersion id) throws IOException {
    ApiAddress address = ApiAddress.builder("dois").embedDoi(id.getDoi()).build();
    try {
      return articleApi.requestObject(address, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException("DOI not found: " + id, e);
    }
  }

  private String getTypeOf(RequestedDoiVersion id) throws IOException {
    return (String) getMetadataForDoi(id).get("type");
  }

  private static final ImmutableMap<String, String> REDIRECT_HANDLERS = ImmutableMap.<String, String>builder()
      .put("article", "article")
      .put("figure", "figurePage")
      .put("table", "figurePage")
      // TODO: supp info
      .build();

  private Link getRedirectFor(Site site, RequestedDoiVersion id) throws IOException {
    String handlerName = REDIRECT_HANDLERS.get(getTypeOf(id));
    if (handlerName == null) {
      throw new RuntimeException("Unrecognized type: " + id);
    }
    Link.Factory.PatternBuilder handlerLink = Link.toLocalSite(site)
        .toPattern(requestMappingContextDictionary, handlerName);
    return pointLinkToDoi(handlerLink, id);
  }

  private static Link pointLinkToDoi(Link.Factory.PatternBuilder link, RequestedDoiVersion id) {
    link.addQueryParameter("id", id.getDoi());
    id.getRevisionNumber().ifPresent(revisionNumber ->
        link.addQueryParameter("rev", revisionNumber));
    return link.build();
  }

}
