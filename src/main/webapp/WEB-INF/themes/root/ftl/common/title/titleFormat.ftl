<#--
  Sub-themes may override this file to change the format of the title - for example, if you want a dash between
  the site and page title instead of a colon. Such overrides should leave intact the macro's name and structure
  (i.e., the way it uses siteTitle.ftl and the siteTitle variable).
  -->
<#macro titleFormat pageTitle>
  <#include "siteTitle.ftl" />
  <#if pageTitle?length gt 0>
  ${siteTitle}: ${pageTitle}<#t>
  <#else>
  ${siteTitle}<#t>
  </#if>
</#macro>
