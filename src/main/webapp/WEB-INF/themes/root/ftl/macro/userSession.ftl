<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

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
