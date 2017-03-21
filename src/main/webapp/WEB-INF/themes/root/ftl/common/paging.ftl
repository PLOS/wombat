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

<#-- This is the basic macro that displays a series of numbered page links.
     We use various combinations of it and ellipses below.  -->
<#macro pageLinkRange path first last selected>
  <#list first..last as i>
    <#assign linkClass = (i == selected)?string("number seq active text-color", "number seq text-color") />
    <#if selected == i >

    <#-- TODO: this should really be a span, not an a, but that messes up the styling right now. -->
    <a class="${linkClass}" data-page="${i}">${i}</a>
    <#else>
    <a href="${path}?<@replaceParams parameterMap=parameterMap replacements={"page": i} />" class="${linkClass}"
       data-page="${i}">${i}</a>
    </#if>
  </#list>
</#macro>

<#function min x y>
  <#if (x lt y)><#return x><#else><#return y></#if>
</#function>
<#function max x y>
  <#if (x gt y)><#return x><#else><#return y></#if>
</#function>

<#-- Macro that renders paging links, such as for search results or article lists.
     The arguments are:

     numPages: total number of results pages
     currentPage: the current page (1-based)
     path: the URL path that clicks on the paging links will be sent to
     parameterMap: Map<String, String[]> of all request parameters to their value.

     A request parameter named "page" will be added to any requests generated when
     clicking on the paging links.
  -->
<#macro paging numPages currentPage path parameterMap alwaysShow=false>
  <#if numPages gt 1>
  <nav id="article-pagination" class="nav-pagination">
    <#if currentPage gt 1>
      <a id="prevPageLink" href="${path}?<@replaceParams parameterMap=parameterMap replacements={"page": currentPage - 1} />"
         class="previous-page switch"><span class="icon"></span><span class="icon-text">Previous Page</span>
      </a>
    <#elseif alwaysShow>
      <span id="prevPageLink" class="previous-page switch disabled"><span class="icon"></span><span class="icon-text">Previous Page</span></span>
    </#if>
    <#if numPages lt 10>
      <@pageLinkRange path=path first=1 last=numPages selected=currentPage />
    <#elseif currentPage lte 4>
      <@pageLinkRange path=path first=1 last=max(4, currentPage + 1) selected=currentPage />
      <span class="skip">...</span>
      <@pageLinkRange path=path first=numPages last=numPages selected=currentPage />
    <#else>
      <@pageLinkRange path=path first=1 last=1 selected=currentPage />
      <span class="skip">...</span>
      <#if currentPage lt numPages - 3>
        <@pageLinkRange path=path first=currentPage - 1 last=currentPage + 1 selected=currentPage />
        <span class="skip">...</span>
        <@pageLinkRange path=path first=numPages last=numPages selected=currentPage />
      <#else>
        <@pageLinkRange path=path first=min(currentPage - 1, numPages - 2) last=numPages selected=currentPage />
      </#if>
    </#if>
    <#if currentPage lt numPages>
      <a id="nextPageLink" href="${path}?<@replaceParams parameterMap=parameterMap replacements={"page": currentPage + 1} />"
         class="next-page switch"><span class="icon"></span><span class="icon-text">Next Page</span>
      </a>
    <#elseif alwaysShow>
      <span id="nextPageLink" class="next-page switch disabled"><span class="icon"></span><span class="icon-text">Next Page</span></span>
    </#if>
  </nav>
  </#if>
</#macro>
