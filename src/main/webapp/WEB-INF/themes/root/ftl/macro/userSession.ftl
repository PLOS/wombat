<#--
  Return a boolean value indicating whether the user has a logged-in session.
  -->
<#function isUserLoggedIn>
  <#return Session["SPRING_SECURITY_CONTEXT"]?exists && Session["SPRING_SECURITY_CONTEXT"].authentication.authenticated />
</#function>

<#--
  Return a link to the page being served, which typically will be used as the "page" parameter of a link to the
  "userLogin" handler. This is necessary for the user to be directed back to the current page after logging in.
  -->
<#function getLinkToCurrentPage>
  <#if requestContext.getQueryString()?exists >
    <#return requestContext.getRequestUri() + "?" + requestContext.getQueryString() />
  <#else>
    <#return requestContext.getRequestUri() />
  </#if>
</#function>
