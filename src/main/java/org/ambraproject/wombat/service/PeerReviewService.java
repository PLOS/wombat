package org.ambraproject.wombat.service;

import java.io.IOException;
import java.util.Map;

public interface PeerReviewService {
  String asHtml(Map<String, ?> itemTable) throws IOException;
}
