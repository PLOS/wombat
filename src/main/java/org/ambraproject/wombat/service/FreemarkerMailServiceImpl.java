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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ambraproject.wombat.config.site.Site;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailPreparationException;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Objects;

public class FreemarkerMailServiceImpl implements FreemarkerMailService {

  @Autowired
  private FreeMarkerConfig freeMarkerConfig;

  private Configuration getConfiguration() {
    return freeMarkerConfig.getConfiguration();
  }

  private Template getEmailTemplate(Site site, String type, String filename) throws IOException {
    String path = String.format("%s/ftl/email/%s/%s.ftl", site, type, filename);
    return getConfiguration().getTemplate(path);
  }

  @Override
  public Multipart createContent(Site site, String templateFilename, Model context)
      throws IOException, MessagingException {
    Template textTemplate = getEmailTemplate(site, "txt", templateFilename);
    Template htmlTemplate = getEmailTemplate(site, "html", templateFilename);

    // Create a "text" Multipart message
    Multipart mp = createPartForMultipart(textTemplate, context, "alternative", ContentType.TEXT_PLAIN);

    // Create a "HTML" Multipart message
    Multipart htmlContent = createPartForMultipart(htmlTemplate, context, "related", ContentType.TEXT_HTML);

    BodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent(htmlContent);
    mp.addBodyPart(htmlPart);

    return mp;
  }

  private Multipart createPartForMultipart(Template htmlTemplate, Model context, String multipartType, ContentType contentType)
      throws IOException, MessagingException {
    Multipart multipart = new MimeMultipart(multipartType);
    multipart.addBodyPart(createBodyPart(contentType, htmlTemplate, context));
    return multipart;
  }

  private BodyPart createBodyPart(ContentType contentType, Template htmlTemplate, Model context)
      throws IOException, MessagingException {
    BodyPart htmlPage = new MimeBodyPart();
    String encoding = getConfiguration().getDefaultEncoding();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(0x100);
    Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, encoding));

    htmlTemplate.setOutputEncoding(encoding);
    htmlTemplate.setEncoding(encoding);

    try {
      htmlTemplate.process(context, writer);
    } catch (TemplateException e) {
      throw new MailPreparationException("Can't generate " + contentType.getMimeType() + " subscription mail", e);
    }

    htmlPage.setDataHandler(createBodyPartDataHandler(outputStream.toByteArray(),
        contentType.toString() + "; charset=" + getConfiguration().getDefaultEncoding()));

    return htmlPage;
  }

  private static DataHandler createBodyPartDataHandler(byte[] data, String contentType) {
    Objects.requireNonNull(data);
    Objects.requireNonNull(contentType);
    return new DataHandler(new DataSource() {
      @Override
      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
      }

      @Override
      public OutputStream getOutputStream() throws IOException {
        throw new IOException("Read-only data");
      }

      @Override
      public String getContentType() {
        return contentType;
      }

      @Override
      public String getName() {
        return "main";
      }
    });
  }

}
