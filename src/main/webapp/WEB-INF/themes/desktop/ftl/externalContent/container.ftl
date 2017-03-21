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

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign title = '' />
<#assign cssFile="site-content.css"/>
<#include "../common/customHeadTags.ftl" />

<#if externalData??>
  <#if externalData.css_sources??>
    <#list externalData.css_sources as css_source>
      <@addCustomHeadTag>
      <link rel="stylesheet" type="text/css" href="<@siteLink path='indirect/'/>${css_source}"/>
      </@addCustomHeadTag> </#list>
  </#if>
</#if>

<@addCustomHeadTag>
<meta name="asset-url-prefix" content="<@siteLink path='indirect/'/>">
</@addCustomHeadTag>

<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<#if externalData.content??>
  ${externalData.content}
</#if>


<#if externalData.name??>
  <div id="external-content-container" data-service="${externalServiceName}" data-${externalData.name}-container> </div>
</#if>

<#include "../common/footer/footer.ftl" />

<@renderJs />

<#if externalData.js_sources??>
  <#list externalData.js_sources as js_source>
    <script src="<@siteLink path='indirect/'/>${js_source}" type="text/javascript"></script>
  </#list>
</#if>


</body>
</html>
