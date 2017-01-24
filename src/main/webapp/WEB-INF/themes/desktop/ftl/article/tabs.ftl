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

<#include "tabSettings.ftl" />

<#--
  Defines a macro for printing the tab links at the top of an article page.
  The invoking page should call displayTabList, passing the current page. (tabyLink is private)
  -->
<#macro tabyLink tabPage section handlerName usesRevision>
  <#if tabDisplaySetting(section)>
    <@themeConfig map="mappings" value=handlerName ; mappingFlag>
      <#if !(mappingFlag?? && mappingFlag.disabled?? && mappingFlag.disabled)>
      <li class="tab-title <#if tabPage == section>active</#if>" id="tab${section}">
        <#assign tabCounter = tabCounter + 1 />
        <#if usesRevision>
          <#assign linkParameters = articlePtr>
        <#else>
          <#assign linkParameters = {"id": article.doi}>
        </#if>
        <@siteLink handlerName=handlerName queryParameters=linkParameters ; href>
          <a href="${href}" class="article-tab-${tabCounter?c}"><#nested/></a>
        </@siteLink>
      </li>
      </#if>
    </@themeConfig>
  </#if>
</#macro>

<#macro displayTabList tabPage='Article'>
<ul class="article-tabs">
  <#assign tabCounter = 0 /> <#-- Referred to privately by tabyLink -->

  <@tabyLink tabPage "Article" "article" true>Article</@tabyLink>

  <#if authors?? && authors?size gt 0>
    <#if !["Correction", "Retraction", "Expression of Concern", "Obituary"]?seq_contains(articleType.name) >
      <@tabyLink tabPage "Authors" "articleAuthors" true>Authors</@tabyLink>
    </#if>
  </#if>

  <@tabyLink tabPage "Metrics" "articleMetrics" false>Metrics</@tabyLink>
  <@tabyLink tabPage "Comments" "articleComments" false>Comments</@tabyLink>
  <@tabyLink tabPage "Related" "articleRelatedContent" false>Related Content</@tabyLink>
</ul>
</#macro>
