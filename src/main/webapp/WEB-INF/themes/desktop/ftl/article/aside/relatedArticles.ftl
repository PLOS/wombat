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


<#if relatedArticles?size gt 0>

<div class="related-articles-container">
  <h3><#include "relatedArticleTitle.ftl"/></h3>
  <ul class="related-articles">
  <#list relatedArticles as relatedArticle>
      <li>
        <@xform xml=relatedArticle.title/>
        <ul class="related-article-links">
          <li class="related-article-link-page">
            <a href="<@siteLink handlerName="article" journalKey=relatedArticle.journal.journalKey
                queryParameters={"id": relatedArticle.doi} />">
              View Page
            </a>
          </li>
          <!--  Annotations and issue images do not have printables. They can be filtered by doi. -->
          <#if relatedArticle.doi?contains("10.1371/journal")>
            <li class="related-article-link-download">
              <a href="<@siteLink handlerName="assetFile" journalKey=relatedArticle.journal.journalKey
                  queryParameters={"type": "printable", "id": relatedArticle.doi} />"
                  target="_blank" title="PDF opens in new window">
                PDF
              </a>
          </li>
        </#if>
        </ul>
      </li>
  </#list>
  </ul>
</div>

</#if>
