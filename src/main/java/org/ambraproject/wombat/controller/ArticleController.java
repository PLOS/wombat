package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SoaService;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController {

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(String doi) {
      super(String.format("Article %s not found", doi));
    }
  }

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
    String path = request.getServletPath();
    Matcher m = ARTICLE_ID_PATTERN.matcher(path);
    if (!m.matches()) {
      throw new IllegalArgumentException();
    }
    return m.group(1);
  }

  @RequestMapping("/{journal}/article")
  public String renderArticle(Model model,
                              @PathVariable("journal") String journal,
                              @RequestParam("doi") String articleId)
      throws IOException {
    String xmlAssetPath = "assetfiles/" + articleId + ".xml";

    Map<?, ?> articleMetadata;
    try {
      articleMetadata = soaService.requestObject("articles/" + articleId, Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }

    StringWriter articleHtml = new StringWriter(BUFFER_SIZE);
    Closer closer = Closer.create();
    try {
      InputStream articleXml;
      try {
        articleXml = closer.register(new BufferedInputStream(soaService.requestStream(xmlAssetPath)));
      } catch (EntityNotFoundException enfe) {
        throw new ArticleNotFoundException(articleId);
      }
      OutputStream outputStream = closer.register(new WriterOutputStream(articleHtml, CHARSET));
      articleTransformService.transform(journal, articleXml, outputStream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }

    model.addAttribute("article", articleMetadata);
    model.addAttribute("articleText", articleHtml.toString());
    return journal + "/ftl/article/article";
  }

}
