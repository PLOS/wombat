<#include "../../common/akitaUrl.ftl" />

<#macro userInfoLink user class="">
  <a class="${class}" href="${akitaUrl('people/${user.displayname}')}">${user.displayname}</a>
</#macro>
