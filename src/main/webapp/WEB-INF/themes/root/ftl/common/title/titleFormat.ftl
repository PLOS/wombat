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
  Sub-themes may override this file to change the format of the title - for example, if you want a dash between
  the site and page title instead of a colon. Such overrides should leave intact the macro's name and structure
  (i.e., the way it uses siteTitle.ftl and the siteTitle variable).
  -->
<#macro titleFormat pageTitle>
  <#include "siteTitle.ftl" />
  <#include "defaultPageTitle.ftl" />
  <#if pageTitle?length gt 0>
    <#if !isArticlePage??>${siteTitle}: </#if>${pageTitle}<#t>
  <#elseif (defaultPageTitle?length > 0) >
    <#if !isArticlePage??>${siteTitle}: </#if>${defaultPageTitle}<#t>
  <#else>
  ${siteTitle}<#t>
  </#if>
</#macro>
