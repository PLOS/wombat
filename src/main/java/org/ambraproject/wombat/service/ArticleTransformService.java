package org.ambraproject.wombat.service;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ArticleTransformService {

  public abstract void transform(String journalKey, String articleId, InputStream xml, OutputStream html)
      throws IOException, TransformerException;

}
