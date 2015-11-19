package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.springframework.ui.Model;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;

public interface FreemarkerMailService {

  /**
   * Build the content of an email message from a FreeMarker template.
   *
   * @param site             the site whose theme to use to get the template
   * @param templateFilename a file path (no extension) in the site's theme at the path {@code ftl/email/(html|txt)/}
   * @param context          the data to inject into the
   * @return the email content, containing HTML and plain text
   * @throws IOException
   * @throws MessagingException
   */
  Multipart createContent(Site site, String templateFilename, Model context)
      throws IOException, MessagingException;

}
