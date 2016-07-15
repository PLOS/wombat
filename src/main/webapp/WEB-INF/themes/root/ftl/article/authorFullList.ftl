<#include "authorItem.ftl" />
<dl>
<#list authors as author><#-- Before the expander -->
        <@authorItemFull author author_index author_has_next true false false/>
    </#list>
</dl>


<#if correspondingAuthors?? && correspondingAuthors?size gt 0>
  <#if correspondingAuthors?size == 1>
  <h2>Corresponding Author</h2>
  <p class="about-author">${correspondingAuthors[0]}</p>
  <#else>
  <h2>Corresponding Authors</h2>
  <ul>
    <#list correspondingAuthors as correspondingAuthor>
      <li class="about-author">${correspondingAuthor}</li>
    </#list>
  </ul>
  </#if>
</#if>

<#list authorListAffiliationMap?keys as affiliation>
<p>
  <span class="author-list">${authorListAffiliationMap[affiliation]}</span>
  <br/>
${affiliation}
</p>
</#list>
