<#macro userInfoLink user class="">

  <@siteLink handlerName="userInfo" pathVariables={"displayName": user.displayname} ; user_href>
  <a class="${class}" href="${user_href}">${user.displayname}</a>
  </@siteLink>
</#macro>
