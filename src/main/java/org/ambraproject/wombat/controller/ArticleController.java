package org.ambraproject.wombat.controller;

import com.google.common.io.Closer;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.SoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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


  private static final Pattern ARTICLE_ID_PATTERN = Pattern.compile(".*?/article/(.*)");

  private static String parseArticlePath(HttpServletRequest request) {
    Matcher m = ARTICLE_ID_PATTERN.matcher(request.getServletPath());
    if (!m.matches()) {
      throw new IllegalArgumentException();
    }
    return m.group(1);
  }

  /**
   * For now, just stream the raw article HTML into the response.
   */
  @RequestMapping("/{journal}/article/**")
  public void renderArticle(HttpServletRequest request, HttpServletResponse response,
                            @PathVariable("journal") String journal)
      throws IOException {
    String articleId = parseArticlePath(request);
    String xmlAssetPath = "assetfiles/" + articleId + ".xml";

    Closer closer = Closer.create();
    try {
      InputStream articleXml = closer.register(new BufferedInputStream(
          soaService.requestStream(xmlAssetPath)));
      OutputStream servletOutputStream = closer.register(new BufferedOutputStream(
          response.getOutputStream()));
      articleTransformService.transform(journal, articleXml, servletOutputStream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

}
