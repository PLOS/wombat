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

package org.ambraproject.wombat.service.remote;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.freemarker.HtmlElementSubstitution;
import org.ambraproject.wombat.freemarker.HtmlElementTransformation;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

public class EditorialContentApi {

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  private Storage storage;

  @Autowired
  private Gson gson;

  long PRESIGNED_URL_EXPIRY = 1; // HOUR

  public boolean objectExists(String key) {
    BlobId blobId = BlobId.of(runtimeConfiguration.getEditorialBucket(), key);
    return storage.get(blobId).exists();
  }

  public URL getPublicUrl(String key) {
    BlobId blobId = BlobId.of(runtimeConfiguration.getEditorialBucket(), key);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    return storage.signUrl(blobInfo, PRESIGNED_URL_EXPIRY, TimeUnit.HOURS, Storage.SignUrlOption.withV4Signature());
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Applies transforms to HTML attributes and performs substitution of placeholder HTML elements with stored content
   */
  public Reader readHtml(final SitePageContext sitePageContext, String pageType, String key,
                         final Set<HtmlElementTransformation> transformations,
                         final Collection<HtmlElementSubstitution> substitutions)
          throws IOException {
    BlobId blobId = BlobId.of(runtimeConfiguration.getEditorialBucket(), key);
    // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this
    // awkward.
    // The whole document will be in memory anyway, so buffering it into a string is no great
    // performance loss.
    String htmlString = IOUtils.toString(storage.readAllBytes(blobId), "UTF-8");
    Document document = Jsoup.parseBodyFragment(htmlString);

    for (HtmlElementTransformation transformation : transformations) {
      transformation.apply(sitePageContext, siteSet, document);
    }
    for (HtmlElementSubstitution substitution : substitutions) {
      substitution.substitute(document);
    }

    // We received a snippet, which Jsoup has automatically turned into a complete HTML document.
    // We want to return only the transformed snippet, so retrieve it from the body tag.
    String transformedHtml = document.getElementsByTag("body").html();
    return new StringReader(transformedHtml);
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Returns a JSON object from a remote service
   */
  public Object getJson(String key) throws IOException {
    BlobId blobId = BlobId.of(runtimeConfiguration.getEditorialBucket(), key);
    String jsonString = IOUtils.toString(storage.readAllBytes(blobId), "UTF-8");
    return gson.fromJson(jsonString, Object.class);
  }
}
