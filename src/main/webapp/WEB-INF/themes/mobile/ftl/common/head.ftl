
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
