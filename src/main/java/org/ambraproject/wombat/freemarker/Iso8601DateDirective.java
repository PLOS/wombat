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

package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * FreeMarker custom directive that parses a ISO 8601 date representation and formats it appropriately.
 * <p>
 * This directive accepts the following parameters:
 * - date (required): a date string in the ISO 8601 format
 * - format (required): format string to use for output
 */
public class Iso8601DateDirective implements TemplateDirectiveModel {

  public static final ZoneId GMT = ZoneId.of("GMT");

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (params.get("date") == null) {
      throw new TemplateModelException("date parameter is required");
    }
    String jsonDate = params.get("date").toString();
    if (params.get("format") == null) {
      throw new TemplateModelException("format parameter is required");
    }
    DateTimeFormatter format = DateTimeFormatter.ofPattern(params.get("format").toString());

    String formattedDate = (jsonDate.length() <= 10)
        ? LocalDate.parse(jsonDate).format(format)
        : Instant.parse(jsonDate).atZone(GMT).format(format);
    environment.getOut().write(formattedDate);
  }
}