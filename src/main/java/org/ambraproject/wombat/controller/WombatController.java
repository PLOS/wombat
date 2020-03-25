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

package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class with common functionality for all controllers in the application.
 */
public abstract class WombatController {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  /**
   * Check that a request parameter is not empty.
   * <p/>
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
   * <p/>
   * The empty string is true because it represents a URL parameter as being present but with no value, e.g. {@code
   * http://example.com/page?foo}. Contrast {@link Boolean#valueOf(String)}, which returns false for the empty string.
   *
   * @param parameterValue a URL parameter value
   * @return the boolean value
   */
  protected static boolean booleanParameter(String parameterValue) {
    return (parameterValue != null) && !Boolean.toString(false).equalsIgnoreCase(parameterValue);
  }


  // Inconsistent with equals. See Javadoc for java.util.SortedSet.
  private static ImmutableSortedSet<String> caseInsensitiveImmutableSet(String... strings) {
    return ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, Arrays.asList(strings));
  }

  /**
   * Names of headers that, on a request from the client, should be passed through on our request to the service tier
   * (Rhino or Content Repo).
   */
  protected static final ImmutableSet<String> ASSET_REQUEST_HEADER_WHITELIST = caseInsensitiveImmutableSet(
      HttpHeaders.IF_MODIFIED_SINCE);

  /**
   * If any validation errors from a form are present, set them up to be rendered.
   * <p>
   * If this method returns {@code true}, it generally means that the calling controller should halt and render a page
   * displaying the validation messages.
   *
   * @param response             the response
   * @param model                the model
   * @param validationErrorNames attribute names for present validation errors
   * @return {@code true} if a validation error is present
   */
  protected static boolean applyValidation(HttpServletResponse response, Model model,
                                           Collection<String> validationErrorNames) {
    if (validationErrorNames.isEmpty()) return false;

    /*
     * Presently, it is assumed that all validation error messages in FreeMarker use a simple presence/absence check
     * with the '??' operator. The value 'true' is just a placeholder. If any validation error messages require more
     * specific values, they must be added to the model separately. Refactor this method if that happens too often.
     */
    validationErrorNames.forEach(error -> model.addAttribute(error, true));

    response.setStatus(HttpStatus.BAD_REQUEST.value());
    return true;
  }

  protected static int getFeedLength(Site site) throws IOException {
    Map<String, Object> feedConfig = site.getTheme().getConfigMap("feed");
    Number length = (Number) feedConfig.get("length");
    return length.intValue();
  }

}
