package org.ambraproject.wombat.service;

import java.util.Map;

public interface CitationDownloadService {

  public abstract String buildRisCitation(Map<String, ?> articleMetadata);

  public abstract String buildBibtexCitation(Map<String, ?> articleMetadata);

}
