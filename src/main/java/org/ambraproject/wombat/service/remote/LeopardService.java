package org.ambraproject.wombat.service.remote;

import java.io.IOException;
import java.io.Reader;

public interface LeopardService {

  public abstract Reader readHtml(String path) throws IOException;

}
