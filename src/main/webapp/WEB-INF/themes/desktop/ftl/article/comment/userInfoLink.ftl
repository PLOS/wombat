<#macro userInfoLink user class="">

  <#--
      TODO: Provide working userId
      The controller currently expects an authIds, but it is uncertain whether it is safe to expose it.
      As a security precaution, suppress authId until the question is resolved.
    -->
  <#assign userId = "" />

  <@siteLink handlerName="userInfo" pathVariables={"authId": userId} ; user_href>
  <a class="${class}" href="${user_href}">${user.displayName}</a>
  </@siteLink>
</#macro>
