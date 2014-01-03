<#-- Fragment that renders paging links, such as for search results or article lists.
     The following variables must be defined:

     numPages: total number of results pages
     currentPage: the current page (1-based)
     path: the URL path that clicks on the paging links will be sent to

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
    <a href="${path}?<@replaceParams params=RequestParameters name="page" value=i />" class="${linkClass}"
       data-page="${i}">${i}</a>
    </#if>
  </#list>
</#macro>
<#function min x y>
  <#if (x < y)><#return x><#else><#return y></#if>
</#function>

<#if numPages gt 1>
<nav id="article-pagination" class="nav-pagination">
  <#if currentPage gt 1>
    <a href="${path}?<@replaceParams params=RequestParameters name="page" value=currentPage - 1 />"
       class="previous switch">Previous Page</a>
  </#if>
  <#if numPages lt 10>
    <@pageLinkRange first=1 last=numPages selected=currentPage />
  <#elseif currentPage lt 4>
    <@pageLinkRange first=1 last=4 selected=currentPage />
    <span class="skip">...</span>
    <@pageLinkRange first=numPages last=numPages selected=currentPage />
  <#else>
    <@pageLinkRange first=1 last=1 selected=currentPage />
    <span class="skip">...</span>
    <#if currentPage lt numPages - 4>
      <@pageLinkRange first=currentPage - 1 last=currentPage + 1 selected=currentPage />
      <span class="skip">...</span>
      <@pageLinkRange first=numPages last=numPages selected=currentPage />
    <#else>
      <@pageLinkRange first=min(currentPage - 1, numPages - 2) last=numPages selected=currentPage />
    </#if>
  </#if>
  <#if currentPage lt numPages>
    <a href="${path}?<@replaceParams params=RequestParameters name="page" value=currentPage + 1 />" class="next switch">Next
      Page</a>
  </#if>
</nav>
</#if>
