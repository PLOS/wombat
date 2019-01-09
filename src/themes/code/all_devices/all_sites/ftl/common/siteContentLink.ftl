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

<#macro siteContentLink link>
<#-- Produce a link to a STATIC PAGE s/*- IN WOMBAT SERVED UP BY THE LEMUR CMS -->
  <#assign siteContentPath><@siteLink handlerName="siteContent" pathVariables={"pageName": link}/></#assign>
  <@menuLink "${siteContentPath}"><#nested></@menuLink>
</#macro>

<#macro siteContentJournalLink link journal>
<#-- Produce a link to a STATIC PAGE static/*- IN THEMES -->
  <#assign siteContentPath><@siteLink handlerName="siteContent" journalKey=journal pathVariables={"pageName": link}/></#assign>
  <@menuLink "${siteContentPath}"><#nested></@menuLink>
</#macro>

<#macro staticContentLink link>
<#-- Produce a link to a STATIC PAGE s/*- IN THEMES -->
  <#assign staticContentPath><@siteLink handlerName="staticPage" pathVariables={"pageName": link}/></#assign>
  <@menuLink "${staticContentPath}"><#nested></@menuLink>
</#macro>

<#macro pageLink handlerName>
<#-- Produce a link to a given handler-->
  <#assign linkPath><@siteLink handlerName="${handlerName}" /></#assign>
  <@menuLink "${linkPath}">
    <#nested>
  </@menuLink>
</#macro>
