package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.util.Map;

public interface IssueService {

  public Map<String, ?> getIssueImage(Site site, String imageArticleDoi) throws IOException;

}
