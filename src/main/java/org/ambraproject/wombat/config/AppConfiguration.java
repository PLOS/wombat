package org.ambraproject.wombat.config;

import org.ambraproject.wombat.rss.ArticleFeedView;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class AppConfiguration extends WebMvcConfigurerAdapter  {
  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    ArticleFeedView articleFeedView = new ArticleFeedView();
    registry.enableContentNegotiation(articleFeedView.getArticleRssView());
    registry.enableContentNegotiation(articleFeedView.getArticleAtomView());
  }
}