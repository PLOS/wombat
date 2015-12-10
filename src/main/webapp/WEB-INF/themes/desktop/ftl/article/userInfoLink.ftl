<#macro userInfoLink user class="">
  <@siteLink handlerName="userInfo" pathVariables={"authId": user.authId} ; user_href>
  <a class="${class}" href="${user_href}">${user.displayName}</a>
  </@siteLink>
</#macro>
