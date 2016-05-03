package org.ambraproject.wombat.service.remote;

public class CorpusContentApi extends AbstractContentApi {

  @Override
  protected String getRepoConfigKey() {
    return "corpus";
  }

}
