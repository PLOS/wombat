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
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
<#include "../macro/removeTags.ftl" />
<#include "title/titleFormat.ftl" />

  <link rel="stylesheet" type="text/css"
        href="https://fonts.googleapis.com/css?family=Open+Sans:400,400i,600,600i">
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
  <#if article.articleType??>
    <meta name="citation_article_type" content="${article.articleType}">
  </#if>
</#if>
  <style type='text/css'>
    @-ms-viewport {
      width: device-width;
    }

    @-o-viewport {
      width: device-width;
    }

    @viewport {
      width: device-width;
    }
  </style>
  <title><@titleFormat removeTags(title) /></title>
<@cssLink target="resource/css/base.css" />
<@cssLink target="resource/css/interface.css" />
<@cssLink target="resource/css/mobile.css" />
<#if cssFile?has_content>
  <@cssLink target="resource/css/${cssFile}" />
</#if>
<#include "../cssLinks.ftl" />

  <script src="<@siteLink path="resource/js/vendor/vendor.min.js" />"></script>
<@js src="resource/js/vendor/underscore-min.js"/>
<@js src="resource/js/vendor/underscore.string.min.js"/>
<@renderCssLinks />
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
<#include "additionalHeadTracking.ftl" />

</head>
