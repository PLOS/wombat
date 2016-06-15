<#assign title = '' />
<#assign cssFile = '' />
<#assign bodyId = '' />
<#assign mainId = '' />
<#assign mainClass = '' />

<#-- Base for the head, all the 'page_header' macros should call this before all the content to get the base tags -->
<!DOCTYPE html>
<!--[if IE 9]><html class="no-js ie9"><![endif]-->
<!--[if gt IE 9]><!-->
<html class="no-js">
<!--<![endif]-->
<#macro base_page_head>

<#include "../common/head.ftl" />
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
  <main<#if mainId?has_content> id="${mainId}"</#if><#if mainClass?has_content> class="${mainClass}"</#if>>
</#macro>

<#-- Extra for 'page_footer', if you need to include anything in the footer before the default content, extend this macro -->
<#macro page_footer_extra>

</#macro>

<#-- the default footer for all the pages, should be called in all the pages after the content, in case of extending this macro, should respect the same structure of this one -->
<#macro page_footer>
    </main>
    <@page_footer_extra />
    <#include "../common/footer/footer.ftl" />

  </div><#-- end container-main -->

    <#include "../common/siteMenu/siteMenu.ftl" />
    <#include "../common/bodyJs.ftl" />

  </body>
  </html>
</#macro>
