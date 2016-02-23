package org.ambraproject.wombat.feed;

import org.springframework.web.servlet.View;

public enum FeedType {
  RSS,
  ATOM;

  public static View getView(AbstractFeedView viewParent, String feedType) {
    if (RSS.name().equalsIgnoreCase(feedType)) return viewParent.getRssView();
    if (ATOM.name().equalsIgnoreCase(feedType)) return viewParent.getAtomView();
    throw new IllegalArgumentException("feedType not matched");
  }

}
