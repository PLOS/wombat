<#macro renderSearchPaginationLinks url totalPages currentPage>
<#--
  realPage is zero based
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
    <#assign realPage = currentPage + 1/>
    <#if (totalPages gt 1 )>
    <div class="pagination">
        <#if (totalPages lt 4) >
            <#if (realPage gt 1) >
                <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=(realPage - 1) />"
                   class="prevPage">&lt;</a>&nbsp;
            <#else>
                <span class="prevPage">&lt;</span>
            </#if>

            <#list 1..totalPages as pageNumber>
                <#if pageNumber == realPage>
                    <strong>${realPage}</strong>
                <#else>
                    <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=(pageNumber - 1) />">${pageNumber}</a>
                </#if>
            </#list>

            <#if (realPage lt totalPages)>
                <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=realPage />"
                   class="nextPage">
                    &gt;</a>
            <#else>
                <span class="nextPage">&gt;</span>
            </#if>
        <#else>
            <#if (realPage gt 1) >
                <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=(realPage - 2) />"
                   class="prevPage">&lt;</a>
                <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=0 />">1</a>
            <#else>
                <span class="prevPage">&lt;</span><strong>1</strong>
            </#if>
            <#if (realPage gt 3) >
                <span>...</span>
            </#if>

        <#--
          Yes the following statements are confusing,
          but it should take care of all the use cases defined at the top
        --->
            <#list min(realPage - 1,0)..max(3,(realPage + 1)) as pageNumber>
                <#if ((pageNumber > 1 && pageNumber < totalPages && pageNumber > (realPage - 2)
                || ((pageNumber == (totalPages - 2)) && (pageNumber > (realPage - 3)))))>
                    <#if (realPage == pageNumber)>
                        <strong>${pageNumber}</strong>
                    <#else>
                        <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=(pageNumber - 1) />">${pageNumber}</a>
                    </#if>
                </#if>
            </#list>
            <#if (realPage lt (totalPages - 2))>
                <span>...</span>
            </#if>
            <#if (realPage lt totalPages)>
                <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=(totalPages - 1)/>">${totalPages}</a>
                <a href="${url}?<@URLParameters resultView=resultView sortOrder=sortOrder page=realPage />" class="nextPage">&gt;</a>
            <#else>
                <strong>${totalPages}</strong>
                <span class="nextPage">&gt;</span>
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
