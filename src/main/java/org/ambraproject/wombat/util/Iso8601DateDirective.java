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

package org.ambraproject.wombat.util;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

/**
 * FreeMarker custom directive that parses a ISO 8601 date representation and formats it appropriately.
 */
public class Iso8601DateDirective implements TemplateDirectiveModel {

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
    String format = params.get("format").toString();
    Calendar calendar = DatatypeConverter.parseDateTime(jsonDate);
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    environment.getOut().write(new SimpleDateFormat(format).format(calendar.getTime()));
  }
}
