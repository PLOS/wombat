package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.SoaService;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController {

  @Autowired
  private ServletContext servletContext;
  @Autowired
  private SoaService soaService;
  @Autowired
  private ArticleTransformService articleTransformService;

  private static final Charset CHARSET = Charsets.UTF_8;
  private static final int BUFFER_SIZE = 0x8000;


  private static final Pattern ARTICLE_ID_PATTERN = Pattern.compile(".*?/article/(.*)");

  private static String parseArticlePath(HttpServletRequest request) {
    Matcher m = ARTICLE_ID_PATTERN.matcher(request.getServletPath());
    if (!m.matches()) {
      throw new IllegalArgumentException();
    }
    return m.group(1);
  }

  @RequestMapping("/{journal}/article/**")
  public String renderArticle(HttpServletRequest request, Model model,
                              @PathVariable("journal") String journal)
      throws IOException {
    String articleId = parseArticlePath(request);
    String xmlAssetPath = "assetfiles/" + articleId + ".xml";

    StringWriter articleHtml = new StringWriter(BUFFER_SIZE);
    Closer closer = Closer.create();
    try {
      InputStream articleXml = closer.register(new BufferedInputStream(
          soaService.requestStream(xmlAssetPath)));
      OutputStream outputStream = closer.register(new WriterOutputStream(articleHtml, CHARSET));
      articleTransformService.transform(journal, articleXml, outputStream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }

    model.addAttribute("articleText", articleHtml.toString());
    return journal + "/ftl/article";
  }

}
