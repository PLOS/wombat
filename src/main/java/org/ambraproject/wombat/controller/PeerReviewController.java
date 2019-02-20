package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Controller for rendering peer review
 */
@Controller
public class PeerReviewController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(PeerReviewController.class);

  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;

  /**
   * Serves the peer review tab for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articlePeerReview", value = "/article/peerReview")
  public String renderArticlePeerReview(HttpServletRequest request, Model model, @SiteParam Site site,
                                        RequestedDoiVersion articleId) throws IOException {

    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articlePeerReview")
        .populate(request, model);

    throwIfPeerReviewNotFound(model.asMap());
    return site + "/ftl/article/peerReview";
  }

  /**
   * Serves the peer review tab with a specific review expanded.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param reviewDoi specifies the review
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "peerReview", value = "/peerReview")
  public String renderPeerReview(HttpServletRequest request, Model model, @SiteParam Site site,
                                 RequestedDoiVersion reviewDoi) throws IOException {

    RequestedDoiVersion articleDoi = getParentArticleDoiFromReviewDoi(reviewDoi);
    articleMetadataFactory.get(site, articleDoi)
        .validateVisibility("peerReview")
        .populate(request, model);

    model.addAttribute("reviewDoi", reviewDoi);
    throwIfPeerReviewNotFound(model.asMap());
    return site + "/ftl/article/peerReview";
  }

  private RequestedDoiVersion getParentArticleDoiFromReviewDoi(RequestedDoiVersion reviewDoiVersion) {
    String reviewDoi = reviewDoiVersion.getDoi();
    // remove the sub-doi (e.g. ".r001") from the end
    String articleDoi = reviewDoi.substring(0, (reviewDoi.length() - 5));
    return reviewDoiVersion.forDoi(articleDoi);
  }

  void throwIfPeerReviewNotFound(Map<String, Object> map) {
    if (null == map.get("peerReview")) {
      throw new NotFoundException();
    }
  }
}
