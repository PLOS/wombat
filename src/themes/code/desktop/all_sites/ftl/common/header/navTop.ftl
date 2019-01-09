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

<#include "../userMgmtUrl.ftl" />

<@navTopItem "https://www.plos.org">plos.org</@navTopItem>

<#include "../../macro/userSession.ftl" />
<#if isUserLoggedIn()>
  <@navTopItem "${userMgmtUrl('account/edit-profile')}">profile</@navTopItem>
  <@appLink path="j_spring_cas_security_logout" ; link>
    <@navTopItem link true>sign out</@navTopItem>
  </@appLink>
<#else>
  <@navTopItem "${userMgmtUrl('registration/new')}">create account</@navTopItem>

  <@siteLink path="user/secure/login?page=${getLinkToCurrentPage()?url('UTF-8')}" ; link>
    <@navTopItem link true>sign in</@navTopItem>
  </@siteLink>
</#if>
