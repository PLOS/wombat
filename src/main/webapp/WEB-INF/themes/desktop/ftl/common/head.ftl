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

<#include "../macro/removeTags.ftl" />
<#include "title/titleFormat.ftl" />
<#--allows for page specific css-->
<#macro pageCSS>

  <#if !cssFile?has_content>
    <#assign cssFile="screen.css"/>
  </#if>

  <@cssLink target="resource/css/${cssFile}"/>

</#macro>

<head prefix="og: http://ogp.me/ns#">
  <title><@titleFormat removeTags(title) /></title>


  <#include "../macro/ifDevFeatureEnabled.ftl" />

<@pageCSS/>


  <!-- allows for  extra head tags -->
  <#if customHeadTags??>
    <@printCustomTags/>
  </#if>

  <#include "extraStyles.ftl"/>

  <#-- hack to put make global vars accessible in javascript. Would be good to come up with a better solution -->
    <script type="text/javascript">
        var siteUrlPrefix = "<@siteLink path=''/>";
    </script>
  <@js target="resource/js/vendor/modernizr-v2.7.1.js" />
  <@js target="resource/js/vendor/detectizr.min.js" />

  <link rel="shortcut icon" href="<@siteLink path="resource/img/favicon.ico"/>" type="image/x-icon"/>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

<#-- // TODO: NEED BACKEND
  <meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}"/>
  <meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}"/>-->


<#if article??>
<#-- // citation meta tags -->
<#include "../article/metaTags.ftl" />
</#if>
<#include "header/adHead.ftl" />
<#-- //references js that is foundational like jquery and foundation.js. JS output is printed at the bottom of the body.
-->
<#include "baseJs.ftl" />
<#include "additionalHeadTracking.ftl" />

</head>


