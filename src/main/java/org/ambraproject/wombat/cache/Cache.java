/*
 * Copyright 2017 Public Library of Science
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

import java.io.Serializable;

/**
 * Interface for a simple key-value cache.  Keys are always Strings while values
 * can be any type that implements {@link java.io.Serializable}.
 */
public interface Cache {

  /**
   * Attempts to retrieve a value from the cache.
   *
   * @param key the cache key
   * @param <T> type of the value
   * @return the value if it is found in the cache, or null if it is not
   */
  <T extends Serializable> T get(String key);

  /**
   * Stores a value in the cache.  The inserted value may have a TTL that is implementation-dependent.
   * Use the three-argument put method if you want to define the TTL.
   *
   * @param key cache key
   * @param value object to store
   */
  <T extends Serializable> void put(String key, T value);

  /**
   * Stores a value in the cache.
   *
   * @param key cache key
   * @param value object to store
   * @param ttl cache TTL for the value, in seconds
   */
  <T extends Serializable> void put(String key, T value, int ttl);

  /**
   * Removes a key/value mapping from the cache, if it exists.
   *
   * @param key cache key
   */
  void remove(String key);
}
