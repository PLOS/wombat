package org.ambraproject.wombat.service.remote;

import java.io.IOException;
import java.io.Reader;

public interface LeopardService {

  /**
   * Read an HTML block from the configured Leopard server.
   *
   * @param path the server's path at which to request an HTML block
   * @return a reader containing the HTML block
   * @throws IOException
   */
  public abstract Reader readHtml(String path) throws IOException;

}
