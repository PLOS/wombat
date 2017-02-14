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

package org.ambraproject.wombat.config.theme;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An object that provides the input needed to construct any number of {@link org.ambraproject.wombat.config.site.Site}
 * and {@link Theme} objects.
 * <p>
 * Multiple theme sources may be combined to create the total set of sites and themes to be used by this system.
 * Although each of those total sets must eventually be non-empty, the collections returned by a single {@code
 * ThemeSource} instance may be empty if they are intended to be combined with another {@code ThemeSource}.
 *
 * @param <T> the subtype of {@link Theme} that is provided by this object (may be equal to {@link Theme} if this object
 *            provides no particular subtype)
 */
public interface ThemeSource<T extends Theme> {

  /**
   * @return a collection of specifications (deserialized JSON input) for sites to be built
   */
  List<Map<String, ?>> readSites();

  /**
   * @return a collection of builders for the themes specified by this source
   */
  Collection<ThemeBuilder<T>> readThemes();

}
