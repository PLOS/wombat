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


<head>
  <meta charset="utf-8">
  <meta name="description" content="">
  <meta name="viewport" content="width=300, initial-scale=1, minimum-scale=1">
<#include "../macro/removeTags.ftl" />
<#include "title/titleFormat.ftl" />

<#if article??>
  <#if article.date??>
    <meta name="citation_date" content="${article.date?date("yyyy-MM-dd")}"/>
  </#if>
  <#if article.title??>
    <meta name="citation_title" content="${article.title?replace('<.+?>',' ','r')?html}"/>
  </#if>
  <#if article.doi??>
    <meta name="citation_doi" content="${article.doi}"/>
  </#if>
  <#if article.abstractText??>
    <#assign articleAbstract><@xform xml=article.abstractText textOnly=true/></#assign>
    <meta name="citation_abstract" content="${articleAbstract}">
  </#if>
  <#if article.articleType??>
    <meta name="citation_article_type" content="${article.articleType}">
  </#if>
</#if>


  <style type='text/css'>
    @-ms-viewport {
      zoom: 1;
      width: auto;
    }

    @-o-viewport {
      zoom: 1;
      width: auto;
    }

    @viewport {
      zoom: 1;
      width: auto;
    }
  </style>
  <title><@titleFormat removeTags(title) /></title>
<@cssLink target="resource/css/base.css" />
<@cssLink target="resource/css/interface.css" />
<@cssLink target="resource/css/mobile.css" />
<#include "../cssLinks.ftl" />
<#if cssFile?has_content>
  <@cssLink target="resource/css/${cssFile}" />
</#if>

  <script src="<@siteLink path="resource/js/vendor/vendor.min.js" />"></script>
<@js src="resource/js/vendor/underscore-min.js"/>
<@js src="resource/js/vendor/underscore.string.min.js"/>
<@renderCssLinks />

<#include "additionalHeadTracking.ftl" />

</head>
