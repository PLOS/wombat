package org.ambraproject.wombat.feed;

import com.google.common.base.CaseFormat;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Values passed into a Spring model to describe the feed.
 */
public enum FeedMetadataField {
  // Required
  SITE, FEED_INPUT,

  // Optional
  ID, TIMESTAMP, TITLE, DESCRIPTION, LINK, IMAGE;

  private String getKey() {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
  }

  public Object getFrom(Map<String, Object> model) {
    return model.get(getKey());
  }

  public Object putInto(Map<String, Object> model, Object value) {
    return model.put(getKey(), value);
  }

  public Object putInto(Model model, Object value) {
    return putInto(model.asMap(), value);
  }

  public Object putInto(ModelAndView mav, Object value) {
    return putInto(mav.getModel(), value);
  }

}