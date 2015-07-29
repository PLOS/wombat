<#-- Fragment that renders paging links, such as for search results or article lists.
     The following variables must be defined:

     numPages: total number of results pages
     currentPage: the current page (1-based)
     path: the URL path that clicks on the paging links will be sent to
     parameterMap: Map<String, String[]> of all request parameters to their value.

     A request parameter named "page" will be added to any requests generated when
     clicking on the paging links.                                              -->

<#-- This is the basic macro that displays a series of numbered page links.
     We use various combinations of it and ellipses below.  -->
<#macro pageLinkRange first last selected>
  <#list first..last as i>
    <#assign linkClass = (i == selected)?string("number seq active text-color", "number seq text-color") />
    <#if selected == i >

    <#-- TODO: this should really be a span, not an a, but that messes up the styling right now. -->
    <a class="${linkClass}" data-page="${i}">${i}</a>
    <#else>
    <a href="${path}?<@replaceParams parameterMap=parameterMap name="page" value=i />" class="${linkClass}"
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

<#if numPages gt 1>
<nav id="article-pagination" class="nav-pagination">
  <#if currentPage gt 1>
    <a id="prevPageLink" href="${path}?<@replaceParams parameterMap=parameterMap name="page" value=currentPage - 1 />"
       class="previous-page switch"><span class="icon"></span><span class="icon-text">Previous Page</span>
    </a>
  <#elseif alwaysShow == "true">
      <span id="prevPageLink" class="previous-page switch disabled"><span class="icon"></span><span class="icon-text">Previous Page</span></span>
  </#if>
  <#if numPages lt 10>
    <@pageLinkRange first=1 last=numPages selected=currentPage />
  <#elseif currentPage lte 4>
    <@pageLinkRange first=1 last=max(4, currentPage + 1) selected=currentPage />
    <span class="skip">...</span>
    <@pageLinkRange first=numPages last=numPages selected=currentPage />
  <#else>
    <@pageLinkRange first=1 last=1 selected=currentPage />
    <span class="skip">...</span>
    <#if currentPage lt numPages - 3>
      <@pageLinkRange first=currentPage - 1 last=currentPage + 1 selected=currentPage />
      <span class="skip">...</span>
      <@pageLinkRange first=numPages last=numPages selected=currentPage />
    <#else>
      <@pageLinkRange first=min(currentPage - 1, numPages - 2) last=numPages selected=currentPage />
    </#if>
  </#if>
  <#if currentPage lt numPages>
    <a id="nextPageLink" href="${path}?<@replaceParams parameterMap=parameterMap name="page" value=currentPage + 1 />"
       class="next-page switch"><span class="icon"></span><span class="icon-text">Next Page</span>
    </a>
  <#elseif alwaysShow == "true">
       <span id="nextPageLink" class="next-page switch disabled"><span class="icon"></span><span class="icon-text">Next Page</span></span>
  </#if>
</nav>
</#if>
