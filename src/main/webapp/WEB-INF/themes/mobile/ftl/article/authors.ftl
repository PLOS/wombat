<#include "../baseTemplates/articleSection.ftl" />

<#macro page_content>
<div id="author-content" class="content">
  <#list authorListAffiliationMap?keys as affiliation>
    <div class="about-author">
      <h3 class="comments-header">${authorListAffiliationMap[affiliation]}</h3>

      <p>${affiliation}</p></div>
  </#list>

  <#if correspondingAuthors?? && correspondingAuthors?size gt 0>
    <#if correspondingAuthors?size == 1>
      <h2>Corresponding Author</h2>
      <p class="about-author">${correspondingAuthors[0]}</p>
    <#else>
      <h2>Corresponding Authors</h2>
      <ul class="bulletlist">
        <#list correspondingAuthors as correspondingAuthor>
          <li class="about-author">${correspondingAuthor}</li>
        </#list>
      </ul>
    </#if>
  </#if>
</div>
<!--end content-->
</#macro>

<@render_page '' 'PLOS - Author Info' 'page-authors' />