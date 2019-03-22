package org.ambraproject.wombat.service;

import java.io.IOException;
import java.util.Map;

public interface PeerReviewService {

  /**
   * Given an article's items, generates an HTML snippet representing the Peer Review tab of an article page.
   * @param itemTable a list of article items as per ArticleService.getItemTable
   * @param citationStr
   * @return an HTML snippet
   * @throws IOException
   */
  String asHtml(Map<String, ?> itemTable, String citationStr) throws IOException;
}
