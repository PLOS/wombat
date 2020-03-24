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

package org.ambraproject.wombat.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.typeadapters.UtcDateTypeAdapter;

import org.ambraproject.wombat.cache.Cache;
import org.ambraproject.wombat.cache.MemcacheClient;
import org.ambraproject.wombat.cache.NullCache;
import org.ambraproject.wombat.config.yaml.IgnoreMissingPropertyConstructor;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ArticleApiImpl;
import org.ambraproject.wombat.service.remote.RemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.ReaderService;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.ambraproject.wombat.service.remote.StreamService;
import org.ambraproject.wombat.service.remote.UserApi;
import org.ambraproject.wombat.service.remote.UserApiImpl;
import org.ambraproject.wombat.util.JodaTimeLocalDateAdapter;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Configuration
public class RootConfiguration {

  @Value("${rootConfiguration.ignoreMissingProperty:false}")
  private boolean ignoreMissingProperty;

  @Bean
  public Yaml yaml() {
    final Yaml yaml;
    if (ignoreMissingProperty) {
      final Constructor contructor = new IgnoreMissingPropertyConstructor();
      yaml = new Yaml(contructor);
    } else {
      yaml = new Yaml();
    }
    return yaml;
  }

  private static final String MEMCACHE_PREFIX = "wombat";
  private static final int MEMCACHE_TTL = 60 * 60;
  
  @Bean
  public ArticleApi articleApi() {
    return new ArticleApiImpl();
  }

  @Bean
  public Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new UtcDateTypeAdapter());
    builder.registerTypeAdapter(org.joda.time.LocalDate.class, JodaTimeLocalDateAdapter.INSTANCE);
    return builder.create();
  }

  @Bean
  public HttpClientConnectionManager httpClientConnectionManager(RuntimeConfiguration runtimeConfiguration) {
    PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

    manager.setMaxTotal(1000);
    manager.setDefaultMaxPerRoute(1000);

    return manager;
  }

  @Bean
  public Cache cache(RuntimeConfiguration runtimeConfiguration) throws IOException {
    if (!Strings.isNullOrEmpty(runtimeConfiguration.getMemcachedServer())) {
      String[] parts  = runtimeConfiguration.getMemcachedServer().split(":");
      MemcacheClient result = new MemcacheClient(parts[0],
                                                 Integer.parseInt(parts[1]),
                                                 MEMCACHE_PREFIX,
                                                 MEMCACHE_TTL);
      result.connect();
      return result;
    } else {
      return new NullCache();
    }
  }

  @Bean
  public SolrSearchApi searchService() {
    return new SolrSearchApiImpl();
  }

  @Bean
  public UserApi userApi() {
    return new UserApiImpl();
  }

  @Bean
  public JsonService jsonService() {
    return new JsonService();
  }

  @Bean
  public AmazonS3 amazonS3(RuntimeConfiguration runtimeConfiguration) {
    String role = runtimeConfiguration.getAwsRoleArn();
    AWSCredentialsProvider credentialsProvider =
        new STSAssumeRoleSessionCredentialsProvider.Builder(role,
            java.util.UUID.randomUUID().toString()).build();
    return new AmazonS3Client(credentialsProvider);
  }

  @Bean
  public RemoteService<InputStream> remoteStreamer(HttpClientConnectionManager httpClientConnectionManager) {
    return new StreamService(httpClientConnectionManager);
  }

  @Bean
  public RemoteService<Reader> remoteReader(HttpClientConnectionManager httpClientConnectionManager) {
    return new ReaderService(httpClientConnectionManager);
  }
}
