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
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
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
  private AmazonS3 amazonS3;

  @Autowired
  private Gson gson;

  long PRESIGNED_URL_EXPIRY = 1000 * 60 * 60; // 1 hour

  public boolean objectExists(String key) {
    return amazonS3.doesObjectExist(runtimeConfiguration.getEditorialBucket(), key);
  }

  public URL getPublicUrl(String key) {
    Date expiration = new Date();
    long expTimeMillis = expiration.getTime() + PRESIGNED_URL_EXPIRY;
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
      new GeneratePresignedUrlRequest(runtimeConfiguration.getEditorialBucket(), key)
      .withMethod(HttpMethod.GET)
      .withExpiration(expiration);
    return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
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
    GetObjectRequest request = new GetObjectRequest(runtimeConfiguration.getEditorialBucket(), key);
    S3Object object = amazonS3.getObject(request);
    // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this
    // awkward.
    // The whole document will be in memory anyway, so buffering it into a string is no great
    // performance loss.
    String htmlString = IOUtils.toString(object.getObjectContent());
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
    GetObjectRequest request = new GetObjectRequest(runtimeConfiguration.getEditorialBucket(), key);
    S3Object object = amazonS3.getObject(request);
    return gson.fromJson(new InputStreamReader(object.getObjectContent()), Object.class);
  }
}
