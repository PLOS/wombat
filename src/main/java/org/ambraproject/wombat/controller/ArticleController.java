package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
import org.ambraproject.wombat.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController {

  @Autowired
  private ArticleService articleService;

  private static final Charset CHARSET = Charsets.UTF_8;

  @RequestMapping("/{journal}/article")
  public String renderArticle(Model model,
                              @PathVariable("journal") String journal,
                              @RequestParam("doi") String articleId)
      throws IOException {
    articleService.requestArticleData(model, articleId, journal, CHARSET);
    return journal + "/ftl/article/article";
  }

}
