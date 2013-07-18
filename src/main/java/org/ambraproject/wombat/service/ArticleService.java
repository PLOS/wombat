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

import org.springframework.ui.Model;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Handles article-level operations on the underlying backend API.
 */
public interface ArticleService {

  /**
   * Adds all data about an article to the model object needed by the view layer.
   *
   * @param model model to be passed to the view for article display
   * @param doi identifies the article
   * @param journal journal containing the article
   * @param charset character set used in the response
   * @throws IOException
   */
  public void requestArticleData(Model model, String doi, String journal, Charset charset) throws IOException;
}
