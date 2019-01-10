<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<nav id="article-post-options">
<#include "submissionLinks.ftl"/>
</nav>
<#include "../common/twitterConfig.ftl"/>
<#macro tweetStream dataWidgetId query height >
  <#include "../common/title/siteTitle.ftl" />
<section id="home-tweets" class="tweets">
  <div class="tweet-heading clearfix">
    <span class="tweet-icon"></span>

    <h2 class="tweet-heading">Tweets</h2>
    <a href="https://twitter.com/search?q=${query}"
       class="tweet-handle">@PLOS</a>
  </div>
  <div class="tweets-container">
    <a class="twitter-timeline"
       href="https://twitter.com/search?q=${query}+lang%3Aen"
       data-widget-id="${dataWidgetId}" data-chrome="noheader nofooter noborders noscrollbar transparent"
       data-tweet-limit="2">Tweets about "${siteTitle}"</a>
    <script>!function (d, s, id) {
      var js, fjs = d.getElementsByTagName(s)[0], p = /^http:/.test(d.location) ? 'http' : 'https';
      if (!d.getElementById(id)) {
        js = d.createElement(s);
        js.id = id;
        js.src = p + "://platform.twitter.com/widgets.js";
        fjs.parentNode.insertBefore(js, fjs);
      }
    }(document, "script", "twitter-wjs");</script>
  </div>
</section><#--end home tweets-->
</#macro>
<@tweetStream "${twitterWidgetId}" "${twitterWidgetQuery}" "${twitterWidgetHeight!0}"/>

<nav id="home-bottom-article-menu" class="article-menu-bottom">
<#-- Same for all PLOS journals. -->

  <a href="https://www.plos.org/publications/journals/" class="btn-lg">PLOS Journals</a>
  <a href="https://blogs.plos.org/" class="btn-lg med">PLOS Blogs</a>

  <div class="btn-top-container">
    <span class="btn-text">Back to Top</span>
    <a class="btn">Back to Top</a>
  </div>
</nav>
