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

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Simple wrapper around the spymemcached API that implements {@link Cache}.
 */
public class MemcacheClient implements Cache {

  private static final Logger log = LoggerFactory.getLogger(MemcacheClient.class);

  /**
   * Timeout for cache reads, in milliseconds.  If memcached doesn't respond within
   * this length of time, gets will return null.
   */
  // TODO: consider making this configurable.
  private static final int GET_TIMEOUT = 1000;

  private final String host;

  private final int port;

  private final String appPrefix;

  private final int defaultTtl;

  private MemcachedClient client;

  /**
   * Constructor.
   *
   * @param host memcached host
   * @param port memcached port
   * @param appPrefix prefix that will be used for all cache keys.  This should be
   *     a String that is shared by all application servers of a certain type.
   * @param defaultTtl the default cache TTL to use for the two-argument put method.
   *     This can be overridden by calling the three-argument put.
   */
  public MemcacheClient(String host, int port, String appPrefix, int defaultTtl) {
    this.host = host;
    this.port = port;
    this.appPrefix = appPrefix;
    this.defaultTtl = defaultTtl;
  }

  /**
   * Connects to the memcached server.  This method must be called at least once
   * before the cache can be used.  It is OK to call it multiple times (it will
   * only connect the first time it's called).
   *
   * @throws IOException
   */
  public synchronized void connect() throws IOException {
    if (client == null) {

      // TODO: in the future we might want to support multiple memcached servers.
      // See http://code.google.com/p/spymemcached/wiki/Examples for how to do this.
      client = new MemcachedClient(new InetSocketAddress(host, port));
    }
  }

  /**
   * Attempts to retrieve a value from the cache.  If the server does not respond
   * within GET_TIMEOUT milliseconds, this method may return null, even if the key
   * was actually in the cache.
   */
  @Override
  public <T extends Serializable> T get(String key) {
    key = buildKey(key);
    Object result = null;
    Future<Object> future = client.asyncGet(key);
    try {
      result = future.get(GET_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {

      // TODO: this makes me nervous.  The idea is that we don't want to block
      // indefinitely for occasional memcached hiccups.  However, if memcached
      // is down completely, we probably want to throw an exception here instead
      // of just log.  One idea would be to keep track of the number of timeouts,
      // and if it exceeds a certain number per time interval, throw an exception
      // instead of logging here.
      log.warn("Memcache lookup timed out for key " + key);
      future.cancel(false);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee);
    }
    return (T) result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Serializable> void put(String key, T value) {
    put(key, value, defaultTtl);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Serializable> void put(String key, T value, int timeout) {
    client.set(buildKey(key), timeout, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String key) {
    client.delete(key);
  }

  private String buildKey(String subKey) {
    return new StringBuilder(appPrefix).append(':').append(subKey).toString();
  }
}
