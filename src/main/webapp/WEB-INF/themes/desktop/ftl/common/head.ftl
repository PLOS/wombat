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

  <@renderCssLinks />

  <!-- allows for  extra head tags -->
  <#if customHeadTags??>
    <@printCustomTags/>
  </#if>

  <#include "extraStyles.ftl"/>

  <#-- hack to put make global vars accessible in javascript. Would be good to come up with a better solution -->
    <script type="text/javascript">
        var siteUrlPrefix = "<@siteLink path=''/>";
    </script>
  <@js src="resource/js/vendor/modernizr-v2.7.1.js" />
  <@js src="resource/js/vendor/detectizr.min.js" />
  <@renderJs />
  <#-- //html5shiv. js and respond.js - enable the use of foundation's dropdowns to work in IE8 -->
  <#-- //The rem  polyfill rewrites the rems in to pixels. I don't think we can call this using the asset manager. -->
  <!--[if IE 8]>
  <script src="<@siteLink path="resource/js/vendor/html5shiv.js"/>"></script>
  <script src="<@siteLink path="resource/js/vendor/respond.min.js"/>"></script>
  <script src="<@siteLink path="resource/js/vendor/rem.min.js"/>"></script>
  <![endif]-->
  <link rel="shortcut icon" href="<@siteLink path="resource/img/favicon.ico"/>" type="image/x-icon"/>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

<#-- // TODO: NEED BACKEND
  <meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}"/>
  <meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}"/>-->


<#if article??>
<#-- // citation meta tags -->
<#include "../article/metaTags.ftl" />
</#if>
<#include "doubleClickAdHead.ftl" />
<#include "header/doubleClickAdHeadSetup.ftl" />
<#-- //references js that is foundational like jquery and foundation.js. JS output is printed at the bottom of the body.
-->
<#include "baseJs.ftl" />
<#include "additionalHeadTracking.ftl" />

</head>


