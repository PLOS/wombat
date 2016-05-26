<#assign title = '' />
<#assign cssFile = '' />
<#assign bodyId = '' />

<#-- Base for the head, all the 'page_header' macros should call this before all the content to get the base tags -->
<#macro base_page_head>
<!DOCTYPE html>
<!--[if IE 9]><html class="no-js ie9"><![endif]-->
<!--[if gt IE 9]><!-->
<html class="no-js">
<!--<![endif]-->
<head>
  <meta charset="utf-8">
  <meta name="description" content="">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <#include "../macro/removeTags.ftl" />
    <#include "../common/title/titleFormat.ftl" />
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
</head>
<body<#if bodyId?has_content> id="${bodyId}"</#if>>
</#macro>
<#-- Extra for 'page_header', if you need to include anything in the header after the default content, extend this macro -->
<#macro page_header_extra>

</#macro>
<#-- the default header for all the pages, should be called in all the pages before the content, in case of extending this macro, should respect the same structure of this one -->
<#macro page_header>
    <@base_page_head />
    <div id="container-main">
    <#include "../common/header/headerContainer.ftl" />
  <@page_header_extra />
</#macro>

<#-- Extra for 'page_footer', if you need to include anything in the footer before the default content, extend this macro -->
<#macro page_footer_extra>

</#macro>

<#-- the default footer for all the pages, should be called in all the pages after the content, in case of extending this macro, should respect the same structure of this one -->
<#macro page_footer>
    <@page_footer_extra />
    <#include "../common/footer/footer.ftl" />

  </div><#-- end container-main -->

    <#include "../common/siteMenu/siteMenu.ftl" />
    <#include "../common/bodyJs.ftl" />

  </body>
  </html>
</#macro>
