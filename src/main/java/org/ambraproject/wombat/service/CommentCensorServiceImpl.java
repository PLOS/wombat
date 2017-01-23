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

package org.ambraproject.wombat.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommentCensorServiceImpl implements CommentCensorService {

  @Autowired
  private SiteSet siteSet;

  private transient Compiled compiled;

  private class Compiled {
    private final ImmutableMap<String, Pattern> patterns;
    private final ImmutableSetMultimap<Site, String> siteCensors;

    private Compiled() {
      Map<Site, Collection<String>> siteCensors = siteSet.getSites().stream()
          .collect(Collectors.toMap(Function.identity(), CommentCensorServiceImpl::getCensoredWordList));
      ImmutableSetMultimap.Builder<Site, String> siteCensorBuilder = ImmutableSetMultimap.builder();
      for (Map.Entry<Site, Collection<String>> entry : siteCensors.entrySet()) {
        siteCensorBuilder.putAll(entry.getKey(), entry.getValue());
      }
      this.siteCensors = siteCensorBuilder.build();

      this.patterns = this.siteCensors
          .values().stream().distinct() // words from all sites, combined
          .collect(ImmutableMap.toImmutableMap(Function.identity(), CommentCensorServiceImpl::compileForWord));
    }
  }

  private static Collection<String> getCensoredWordList(Site site) {
    Map<String, Object> commentConfig = site.getTheme().getConfigMap("comment");
    return (Collection<String>) commentConfig.get("censoredWords");
  }

  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private static Pattern compileForWord(String word) {
    word = WHITESPACE.matcher(word.trim()).replaceAll("\\\\s+");
    return Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public Collection<String> findCensoredWords(Site site, String content) {
    if (Strings.isNullOrEmpty(content)) return ImmutableList.of();
    Compiled compiled = (this.compiled == null) ? (this.compiled = new Compiled()) : this.compiled;
    Set<String> censoredWords = compiled.siteCensors.get(site);
    return censoredWords.parallelStream()
        .filter((String word) -> {
          Pattern pattern = compiled.patterns.get(word);
          return pattern.matcher(content).find();
        })
        .sorted()
        .collect(Collectors.toList());
  }

}
