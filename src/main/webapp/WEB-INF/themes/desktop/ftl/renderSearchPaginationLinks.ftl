<#macro renderSearchPaginationLinks url totalPages currentPageParam>
<#--
  currentPage is zero based
  SOLR (the action class) expects a startPage parameter of 0 to N
  We change this to a nonZero value here to make things a bit more readable

  It supports the following use cases and will output the following:

  Current page is the start or end
  Current Page is 1:
  < 1 2 3  ... 10 >
  Current Page is 10:
  < 1 ...8 9 10 >

  Current page is 5:
  (Current page is greater then 2 pages away from start or end)
  < 1 ...4 5 6 ... 10 >

  Current page is less then 2 pages away from start or end:
  Current Page is 8:
  < 1 ...7 8 9 10 >
  < 1 2 3 4 ... 10 >
-->
    <#assign currentPage = currentPageParam + 1/>

    <#if (totalPages gt 1 )>
    <div class="pagination">
        <#if (totalPages lt 4) >
            <#if (currentPage gt 1) >
                <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage - 2] />"
                   class="prev">&lt;</a>&nbsp;
            <#else>
                <span class="prev">&lt;</span>
            </#if>

            <#list 1..totalPages as pageNumber>
                <#if pageNumber == currentPage>
                    <strong>${currentPage}</strong>
                <#else>
                    <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[pageNumber - 1] />">${pageNumber}</a>
                </#if>
            </#list>

            <#if (currentPage lt totalPages)>
                <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage] />"
                   class="next">
                    &gt;</a>
            <#else>
                <span class="next">&gt;</span>
            </#if>
        <#else>
            <#if (currentPage gt 1) >
                <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage - 2] />"
                   class="prev">&lt;</a>
                <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[0] />">1</a>
            <#else>
                <span class="prev">&lt;</span><strong>1</strong>
            </#if>
            <#if (currentPage gt 3) >
                <span>...</span>
            </#if>

        <#--
          Yes the following statements are confusing,
          but it should take care of all the use cases defined at the top
        --->
            <#list min(currentPage - 1,0)..max(3,(currentPage + 1)) as pageNumber>
                <#if ((pageNumber > 1 && pageNumber < totalPages && pageNumber > (currentPage - 2)
                || ((pageNumber == (totalPages - 2)) && (pageNumber > (currentPage - 3)))))>
                    <#if (currentPage == pageNumber)>
                        <strong>${pageNumber}</strong>
                    <#else>
                        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[pageNumber - 1] />">${pageNumber}</a>
                    </#if>
                </#if>
            </#list>
            <#if (currentPage lt (totalPages - 2))>
                ...
            </#if>
            <#if (currentPage lt totalPages)>
                <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[totalPages - 1] />">${totalPages}</a>
                <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage] />"
                   class="next">&gt;</a>
            <#else>
                <strong>${totalPages}</strong>
                <span class="next">&gt;</span>
            </#if>
        </#if>
    </div>
    </#if>
</#macro>

<#function max x y>
    <#if  (x > y) >
        <#return x />
    <#else>
        <#return y />
    </#if>
</#function>
<#function min x y>
    <#if x < y>
        <#return x />
    <#else>
        <#return y />
    </#if>
</#function>
