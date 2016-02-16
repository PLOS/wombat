<#include "../../common/akitaUrl.ftl" />

<#macro userInfoLink user class="">
  <#assign userInfoUrl = akitaUrl('people/${user.displayname}') />
  <a class="${class}" <#if userInfoUrl?has_content>href="${userInfoUrl}"</#if> >${user.displayname}</a>
</#macro>
