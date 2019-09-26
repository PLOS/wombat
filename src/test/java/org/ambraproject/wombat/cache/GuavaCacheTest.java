/*
 * Copyright 2018 Public Library of Science
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

package org.ambraproject.wombat.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class GuavaCacheTest {
  @Test
  public void testCacheGet() {
    GuavaCache cache = new GuavaCache();
    cache.put("hello", "world");

    assertEquals("world", cache.get("hello"));
  }

  @Test
  public void testCacheRemove() {
    GuavaCache cache = new GuavaCache();
    cache.put("hello", "world");
    cache.remove("hello");
    assertNull(cache.get("hello"));
  }

  @Test
  public void testCacheEviction() {
    GuavaCache cache = new GuavaCache(1);
    cache.put("hello", "world");
    cache.put("goodbye", "world");
    assertNull(cache.get("hello"));
    assertEquals("world", cache.get("goodbye"));
  }
}
