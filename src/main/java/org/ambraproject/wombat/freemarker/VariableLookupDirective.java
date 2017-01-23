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
import java.util.Map;

/**
 * A directive that retrieves a value from the Java tier and provides it to the FreeMarker templating engine, either as
 * raw text or as a FreeMarker variable.
 * <p/>
 * In the first mode, the directive invocation does not define any loop vars and does not have a body (i.e., it is a
 * self-closing tag). Then, the result is written into the page body as the output of the invocation. For example:
 * <pre>
 *   &lt;span>The value is &lt;@myDirective foo="bar" />&lt;/span>
 * </pre>
 * resolves to
 * <pre>
 *   &lt;span>The value is qux&lt;/span>
 * </pre>
 * <p/>
 * In the second mode, the directive defines a loop var and has a body that refers to the loop var. Then the result will
 * be bound to that loop var and the element body will be rendered with that variable, allowing arbitrary FreeMarker
 * logic to be applied to it. For example:
 * <pre>
 *   &lt;@myDirective foo="bar" ; x>
 *     &lt;span>The value is ${x} and its length is ${x?length}&lt;/span>
 *   &lt;/@myDirective>
 * </pre>
 * resolves to
 * <pre>
 *   &lt;span>The value is qux and its length is 3&lt;/span>
 * </pre>
 * <p/>
 * The following are all errors: <ul> <li>{@code <@myDirective foo="bar">Hello!</@myDirective>} (has body but no loop
 * var)</li> <li>{@code <@myDirective foo="bar" ; x />} (has loop var but no body)</li> <li>{@code <@myDirective
 * foo="bar" ; x y>${x + y}</@myDirective>} (has more than one loop var)</li> </ul>
 * <p/>
 * The value may be of any type that the FreeMarker environment's {@link freemarker.template.ObjectWrapper} can consume.
 * The first mode prints the result of the value's Java {@link #toString()} method. The second mode uses the FreeMarker
 * variable from {@code ObjectWrapper}. Null values are allowed only in the second mode.
 *
 * @param <T> the type of value looked up from the Java tier
 */
public abstract class VariableLookupDirective<T> implements TemplateDirectiveModel {

  @Override
  public final void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    T value = getValue(env, params); // may be null
    if (loopVars.length == 0) {
      if (body != null) {
        throw new TemplateModelException("1 loopVar is required if there is a body");
      }
      if (value == null) {
        throw new TemplateModelException("Value is null");
      }
      env.getOut().write(value.toString());
    } else if (loopVars.length == 1) {
      if (body == null) {
        throw new TemplateModelException("Body is required if there is a loopVar");
      }
      // If value is null, let loopVars[0] = null and allow FreeMarker code to check it with '??' operator
      loopVars[0] = env.getObjectWrapper().wrap(value);
      body.render(env.getOut());
    } else {
      throw new TemplateModelException("Does not take more than 1 loopVar");
    }
  }

  /**
   * Return the value to provide as the body or loopvar.
   * <p/>
   * The {@code env} and {@code params} arguments are the same as those specified for {@link
   * TemplateDirectiveModel#execute}. Implementing classes may define their own contracts for the content of {@code
   * params}.
   *
   * @param env    the FreeMarker processing environment
   * @param params the parameters (if any) passed to the directive
   * @return the result
   * @throws TemplateException
   * @throws IOException
   */
  protected abstract T getValue(Environment env, Map params)
      throws TemplateException, IOException;

}
