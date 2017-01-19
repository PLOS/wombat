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

import com.google.common.base.Preconditions;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Generates an insecure random integer, between the params "minValue" (inclusive) and "maxValue" (exclusive).
 */
public class RandomIntegerDirective implements TemplateDirectiveModel {

  private static final Integer DEFAULT_MIN = 0;
  private static final Integer DEFAULT_MAX = 10000;

  private final Random random = new Random(); // needs SecureRandom if these numbers are used cryptographically

  /**
   * Interpret a FreeMarker number as a primitive integer.
   *
   * @param number       a FreeMarker number value
   * @param defaultValue the value to use if {@code number == null}
   * @return the integer value
   * @throws TemplateModelException
   * @throws java.lang.ClassCastException if {@code number} is non-null and not a FreeMarker number
   */
  private static int asInteger(Object number, int defaultValue) throws TemplateModelException {
    if (number == null) return defaultValue;
    return ((TemplateNumberModel) number).getAsNumber().intValue();
  }

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    int minValue = asInteger(params.get("minValue"), DEFAULT_MIN);
    int maxValue = asInteger(params.get("maxValue"), DEFAULT_MAX);
    Preconditions.checkArgument(minValue < maxValue);

    int randomValue = random.nextInt(maxValue - minValue) + minValue;

    env.getOut().write(String.valueOf(randomValue));
  }

}
