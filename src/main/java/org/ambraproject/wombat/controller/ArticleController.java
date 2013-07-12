package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.SoaService;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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
import java.util.Map;
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

  /**
   * Produce a relative path from an article page to the "article" level. The value, used as the {@code href} attribute
   * of a {@code base} element, allows other relative paths in the HTML to be relative to "article".
   * <p/>
   * This is a silly hack around the problem that the article's DOI is part of the article page's URL but may contain a
   * variable number of slashes. For example, a relative path from
   * <pre>http://example.org/ambra/article/info:doi/10.0000/journal.0000000</pre>
   * to
   * <pre>http://example.org/ambra/static/css/base.css</pre>
   * would be
   * <pre>../../../static/css/base.css</pre>
   * But, if the article URL were instead
   * <pre>http://example.org/ambra/article/info:doi/10.0000/journal/volume01/issue01/0000000</pre>
   * (which is a valid DOI, and up to the user) then the relative path must become
   * <pre>../../../../../../static/css/base.css</pre>
   * We want front-end code to be able to contain relative paths without worrying about that.
   * <p/>
   * A real solution would avoid letting user-defined DOIs dictate page structure at all, folding DOIs into a parameter
   * perhaps. But because we seem to need URL compatibility with Ambra, this is in place for now.
   *
   * @param articleId the article's ID (DOI)
   * @return an {@code href} value for the base tag
   */
  private static String baseHref(String articleId) {
    int count = StringUtils.countOccurrencesOf(articleId, "/");
    if (count == 0) {
      return ".";
    }
    StringBuilder buffer = new StringBuilder(3 * count - 1).append("..");
    for (int i = 1; i < count; i++) {
      buffer.append("/..");
    }
    return buffer.toString();
  }

  @RequestMapping("/{journal}/article/**")
  public String renderArticle(HttpServletRequest request, Model model,
                              @PathVariable("journal") String journal)
      throws IOException {
    String articleId = parseArticlePath(request);
    String xmlAssetPath = "assetfiles/" + articleId + ".xml";

    Map<?, ?> articleMetadata = soaService.requestObject("articles/" + articleId, Map.class);

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

    model.addAttribute("baseHref", baseHref(articleId));
    model.addAttribute("article", articleMetadata);
    model.addAttribute("articleText", articleHtml.toString());
    return journal + "/ftl/article";
  }

}
