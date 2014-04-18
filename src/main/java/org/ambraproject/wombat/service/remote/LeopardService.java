package org.ambraproject.wombat.service.remote;

import java.io.IOException;
import java.io.InputStream;

public interface LeopardService {

  InputStream requestStream(String path) throws IOException;

}
