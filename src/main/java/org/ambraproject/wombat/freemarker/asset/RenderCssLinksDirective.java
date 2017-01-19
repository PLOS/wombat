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

package org.ambraproject.wombat.freemarker.asset;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.ambraproject.wombat.service.AssetService;

import java.io.IOException;
import java.util.Map;

/**
 * Freemarker custom directive that renders any CSS links added via calls to {@link CssLinkDirective}. A single instance
 * of this directive should be added at the end of the head element on the page. It will do nothing if we are running in
 * dev assets mode (since the links were already rendered).
 */
public class RenderCssLinksDirective extends RenderAssetsDirective {

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    renderAssets(AssetService.AssetType.CSS, CssLinkDirective.REQUEST_VARIABLE_NAME, environment);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getHtml(String assetPath) {
    return String.format("<link rel=\"stylesheet\" href=\"%s\" />\n", assetPath);
  }
}
