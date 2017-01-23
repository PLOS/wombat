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

package org.ambraproject.wombat.feed;

import com.google.common.base.Strings;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.util.ClientEndpoint;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class FeedMetadata {
  private final Map<String, Object> model;
  private final HttpServletRequest request;
  private final Site site;
  private final Map<String, Object> feedConfig;

  FeedMetadata(Map<String, Object> model, HttpServletRequest request) {
    this.model = Objects.requireNonNull(model);
    this.request = Objects.requireNonNull(request);
    this.site = Objects.requireNonNull((Site) FeedMetadataField.SITE.getFrom(model));
    this.feedConfig = Collections.unmodifiableMap(site.getTheme().getConfigMap("feed"));
  }

  public String buildLink(Function<Link.Factory, Link> linkFunction) {
    return linkFunction.apply(Link.toAbsoluteAddress(site)).get(request);
  }

  public Object getFromFeedConfig(String key) {
    return feedConfig.get(key);
  }


  /**
   * @return a URL that <em>uniquely</em> identifies the feed
   */
  public String getId() {
    String id = (String) FeedMetadataField.ID.getFrom(model);
    return Strings.isNullOrEmpty(id) ? ClientEndpoint.getRequestUrl(request) : id;
  }

  /**
   * @return a timestamp for the latest time the feed was updated, defaulting to now
   */
  public Date getTimestamp() {
    Date timestamp = (Date) FeedMetadataField.TIMESTAMP.getFrom(model);
    return (timestamp == null) ? new Date() : timestamp;
  }

  public String getTitle() {
    String journalName = site.getJournalName();
    String feedTitle = (String) FeedMetadataField.TITLE.getFrom(model);
    return Strings.isNullOrEmpty(feedTitle) ? journalName : journalName + ": " + feedTitle;
  }

  public String getDescription() {
    String feedDescription = (String) FeedMetadataField.DESCRIPTION.getFrom(model);
    if (!Strings.isNullOrEmpty(feedDescription)) {
      return feedDescription;
    }
    String siteDescription = (String) feedConfig.get("description");
    return Strings.nullToEmpty(siteDescription);
  }

  public String getSiteLink() {
    return Link.toAbsoluteAddress(site).toPath("").get(request);
  }

  public String getLink() {
    String feedLink = (String) FeedMetadataField.LINK.getFrom(model);
    return Strings.isNullOrEmpty(feedLink) ? getSiteLink() : feedLink;
  }

  public String getCopyright() {
    return (String) feedConfig.get("copyright");
  }

  public String getImageLink() {
    // An image defined for this particular feed takes precedence
    String imagePath = (String) FeedMetadataField.IMAGE.getFrom(model);
    if (Strings.isNullOrEmpty(imagePath)) {
      // Else, check if the site has configured a global image for all feeds
      imagePath = (String) feedConfig.get("image");
    }

    // If we have a path, resolve it into a link
    return (imagePath == null) ? null
        : Link.toAbsoluteAddress(site).toPath(imagePath).get(request);
  }

  public String getAuthorName() {
    return (String) feedConfig.get("authorName");
  }

  public String getAuthorEmail() {
    return (String) feedConfig.get("authorEmail");
  }
}
