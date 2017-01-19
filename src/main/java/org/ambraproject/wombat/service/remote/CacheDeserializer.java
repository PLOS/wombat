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

package org.ambraproject.wombat.service.remote;

import java.io.Closeable;
import java.io.IOException;

/**
 * How to convert a data stream into a cacheable value.
 *
 * @param <S> the type of stream (typically {@link java.io.InputStream} or {@link java.io.Reader})
 * @param <T> the type of data
 */
@FunctionalInterface
public interface CacheDeserializer<S extends Closeable, T> {

  /**
   * Take a stream that is read in the event of a cache miss and convert it into an object that will be inserted into
   * the cache.
   *
   * @param stream the stream from a remote service
   * @return the stream contents in a cacheable form
   * @throws IOException
   */
  public abstract T read(S stream) throws IOException;

}
