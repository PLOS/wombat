package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.controller.ArticleMetadata;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class IssueServiceImpl implements IssueService {
  private static final Logger log = LoggerFactory.getLogger(IssueServiceImpl.class);

  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;

  public Map<String, ?> getIssueImage(Site site, String imageArticleDoi) throws IOException {
    List<Map<String, ?>> imageArticleFigures = articleMetadataFactory.get(site, RequestedDoiVersion.of(imageArticleDoi))
        .getFigureView();
    if (imageArticleFigures.isEmpty()) {
      throw new RuntimeException("Issue image article has more than no figures: " + imageArticleDoi);
    } else {
      if (imageArticleFigures.size() > 1) {
        log.warn("Issue image article has more than one figure: {}", imageArticleDoi);
      }
      return imageArticleFigures.get(0);
    }
  }
}
