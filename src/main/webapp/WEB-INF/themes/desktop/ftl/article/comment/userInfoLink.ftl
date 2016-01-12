<#macro userInfoLink user class="">

  <@siteLink handlerName="userInfo" pathVariables={"authId": userId} ; user_href>
  <a class="${class}" href="${user_href}">${user.displayName}</a>
  </@siteLink>
</#macro>
