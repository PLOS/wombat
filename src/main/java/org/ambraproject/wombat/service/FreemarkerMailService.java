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

public class FreemarkerMailService {

  @Autowired
  private FreeMarkerConfig freeMarkerConfig;

  private Configuration getConfiguration() {
    return freeMarkerConfig.getConfiguration();
  }

  public Multipart createContent(Site site, String templateFilename, Model context)
      throws IOException, MessagingException {
    Template textTemplate = getConfiguration().getTemplate(site + "/ftl/email/txt/" + templateFilename);
    Template htmlTemplate = getConfiguration().getTemplate(site + "/ftl/email/html/" + templateFilename);

    // Create a "text" Multipart message
    Multipart mp = createPartForMultipart(textTemplate, context, "alternative", ContentType.TEXT_PLAIN);

    // Create a "HTML" Multipart message
    Multipart htmlContent = createPartForMultipart(htmlTemplate, context, "related", ContentType.TEXT_HTML);

    BodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent(htmlContent);
    mp.addBodyPart(htmlPart);

    return mp;
  }

  private Multipart createPartForMultipart(Template htmlTemplate, Model context, String multipartType, ContentType mimeType)
      throws IOException, MessagingException {
    Multipart multipart = new MimeMultipart(multipartType);
    multipart.addBodyPart(createBodyPart(mimeType, htmlTemplate, context));
    return multipart;
  }

  private BodyPart createBodyPart(ContentType mimeType, Template htmlTemplate, Model context)
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
      throw new MailPreparationException("Can't generate " + mimeType + " subscription mail", e);
    }

    htmlPage.setDataHandler(createBodyPartDataHandler(outputStream.toByteArray(),
        mimeType.toString() + "; charset=" + getConfiguration().getDefaultEncoding()));

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
