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

package org.ambraproject.wombat.service;

import com.google.common.io.Closer;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * {@inheritDoc}
 */
public class ArticleServiceImpl implements ArticleService {

  /**
   * Number of bytes we use to buffer responses from the SOA layer.
   */
  private static final int BUFFER_SIZE = 0x8000;

  @Autowired
  private SoaService soaService;

  @Autowired
  private ArticleTransformService articleTransformService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void requestArticleData(Model model, String doi, String journal, Charset charset) throws IOException {
    String xmlAssetPath = "assetfiles/" + doi + ".xml";
    Map<?, ?> articleMetadata;
    try {
      articleMetadata = soaService.requestObject("articles/" + doi, Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(doi);
    }
    StringWriter articleHtml = new StringWriter(BUFFER_SIZE);
    Closer closer = Closer.create();
    try {
      InputStream articleXml;
      try {
        articleXml = closer.register(new BufferedInputStream(soaService.requestStream(xmlAssetPath)));
      } catch (EntityNotFoundException enfe) {
        throw new ArticleNotFoundException(doi);
      }
      OutputStream outputStream = closer.register(new WriterOutputStream(articleHtml, charset));
      articleTransformService.transform(journal, articleXml, outputStream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
    model.addAttribute("article", articleMetadata);
    model.addAttribute("articleText", articleHtml.toString());
    requestCorrections(model, doi);
  }

  /**
   * Checks whether any corrections are associated with the given article, and appends
   * them to the model if so.
   *
   * @param model model to be passed to the view
   * @param doi identifies the article
   * @throws IOException
   */
  private void requestCorrections(Model model, String doi) throws IOException {
    List<?> corrections = soaService.requestObject("corrections/" + doi, List.class);
    if (corrections != null && !corrections.isEmpty()) {
      model.addAttribute("articleCorrections", corrections);
    }
  }
}
