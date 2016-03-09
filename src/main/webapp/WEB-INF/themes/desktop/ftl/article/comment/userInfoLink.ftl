<#include "../../common/userMgmtUrl.ftl" />

<#macro userInfoLink user class="">
  <#if user.displayname?has_content && user.displayname != "not found">
    <#assign userInfoUrl = userMgmtUrl('people/${user.displayname}') />
    <a class="${class}" <#if userInfoUrl?has_content>href="${userInfoUrl}"</#if> >${user.displayname}</a>
  <#else>
    not found
  </#if>
</#macro>
