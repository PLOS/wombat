package org.ambraproject.wombat.service.remote;

import java.io.IOException;

public interface LeopardService {

  /**
   * Read an HTML block from the configured Leopard server, apply transforms to it specific to the Leopard API, then
   * return the transformed HTML.
   *
   * @param path the server's path at which to request an HTML block
   * @return the transformed HTML block
   * @throws IOException
   */
  public abstract String readHtml(String path) throws IOException;

}
