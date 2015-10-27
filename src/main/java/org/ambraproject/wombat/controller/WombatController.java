/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.apache.http.Header;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ambraproject.wombat.util.ReproxyUtil.X_PROXY_CAPABILITIES;
import static org.ambraproject.wombat.util.ReproxyUtil.X_REPROXY_CACHE_FOR;
import static org.ambraproject.wombat.util.ReproxyUtil.X_REPROXY_URL;

/**
 * Base class with common functionality for all controllers in the application.
 */
public abstract class WombatController {

  /**
   * Validate that an article ought to be visible to the user. If not, throw an exception indicating that the user
   * should see a 404.
   * <p>
   * An article may be invisible if it is not in a published state, or if it has not been published in a journal
   * corresponding to the site.
   *
   * @param site            the site on which the article was queried
   * @param articleMetadata the article metadata, or a subset containing the {@code state} and {@code journals} fields
   * @throws NotVisibleException if the article is not visible on the site
   */
  protected void validateArticleVisibility(Site site, Map<?, ?> articleMetadata) {
    String state = (String) articleMetadata.get("state");
    if (!"published".equals(state)) {
      throw new NotVisibleException("Article is in unpublished state: " + state);
    }

    Set<String> articleJournalKeys = ((Map<String, ?>) articleMetadata.get("journals")).keySet();
    String siteJournalKey = site.getJournalKey();
    if (!articleJournalKeys.contains(siteJournalKey)) {
      throw new NotVisibleException("Article is not published in: " + site);
    }
  }

  /**
   * Check that a request parameter is not empty.
   * <p>
   * This is useful for validating that the user didn't supply an empty string as a URL parameter, such as by typing
   * ".../article?doi" into the browser bar when ".../article?id=10.0/foo" is expected. The {@code required} argument on
   * {@code RequestParam} merely guarantees the parameter to be non-null, not non-empty.
   *
   * @param parameter a non-null value supplied as a {@code RequestParam}
   * @throws NotFoundException    if the parameter is empty
   * @throws NullPointerException if the parameter is null
   */
  protected static void requireNonemptyParameter(String parameter) {
    if (parameter.isEmpty()) {
      throw new NotFoundException("Required parameter not supplied");
    }
  }

  /**
   * Interpret a URL parameter as a boolean. In general, interpret {@code null} as false and all non-null strings,
   * including the empty string, as true. But the string {@code "false"} is (case-insensitively) false.
   * <p>
   * The empty string is true because it represents a URL parameter as being present but with no value, e.g. {@code
   * http://example.com/page?foo}. Contrast {@link Boolean#valueOf(String)}, which returns false for the empty string.
   *
   * @param parameterValue a URL parameter value
   * @return the boolean value
   */
  protected static boolean booleanParameter(String parameterValue) {
    return (parameterValue != null) && !Boolean.toString(false).equalsIgnoreCase(parameterValue);
  }

  /**
   * Names of headers that, on a request from the client, should be passed through on our request to the service tier
   * (Rhino or Content Repo).
   */
  protected static final ImmutableSet<String> ASSET_REQUEST_HEADER_WHITELIST = caseInsensitiveImmutableSet(X_PROXY_CAPABILITIES);

  /**
   * Names of headers that, in a response from the service tier (Rhino or Content Repo), should be passed through to the
   * client.
   */
  private static final ImmutableSet<String> ASSET_RESPONSE_HEADER_WHITELIST = caseInsensitiveImmutableSet(
      HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_DISPOSITION, X_REPROXY_URL, X_REPROXY_CACHE_FOR);
  protected static final HttpMessageUtil.HeaderFilter ASSET_RESPONSE_HEADER_FILTER = new HttpMessageUtil.HeaderFilter() {
    @Override
    public String getValue(Header header) {
      String name = header.getName();
      if (!ASSET_RESPONSE_HEADER_WHITELIST.contains(name)) {
        return null;
      }
      String value = header.getValue();
      if (name.equalsIgnoreCase(HttpHeaders.CONTENT_DISPOSITION)) {
        return sanitizeAssetFilename(value);
      }
      return value;
    }
  };


  private static final Pattern BAD_THUMBNAIL_EXTENSION = Pattern.compile("\\.PNG_\\w+$", Pattern.CASE_INSENSITIVE);

  /**
   * Edit a "Content-Disposition" header value by changing a ".PNG_*" file extension to ".png". (The ".PNG_*" file
   * extensions are an ugly system quirk that we don't want to expose to the user.)
   *
   * @param contentDispositionValue a "Content-Disposition" header value
   * @return an edited value if it's bad; else the same value
   */
  private static String sanitizeAssetFilename(String contentDispositionValue) {
    Matcher matcher = BAD_THUMBNAIL_EXTENSION.matcher(contentDispositionValue);
    if (matcher.find()) {
      return matcher.replaceFirst(".png");
    }
    return contentDispositionValue;
  }


  // Inconsistent with equals. See Javadoc for java.util.SortedSet.
  private static ImmutableSortedSet<String> caseInsensitiveImmutableSet(String... strings) {
    return ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, Arrays.asList(strings));
  }
}
