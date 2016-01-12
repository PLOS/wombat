<#macro userInfoLink user class="">

  <@siteLink handlerName="userInfo" pathVariables={"displayName": user.displayName} ; user_href>
  <a class="${class}" href="${user_href}">${user.displayName}</a>
  </@siteLink>
</#macro>
