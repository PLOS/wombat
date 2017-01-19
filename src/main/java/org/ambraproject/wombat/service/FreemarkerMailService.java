/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
