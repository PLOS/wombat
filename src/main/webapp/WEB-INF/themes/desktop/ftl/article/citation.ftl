<#macro displayCitation citation>
  <#list citation.authors as author>
  <span class="citationAuthor">
    ${author.surnames!}
    <#if author.givenNames??><@abbreviatedName>${author.givenNames}</@abbreviatedName></#if>
    <#if author.suffix??>${author.suffix?replace('.', '')}</#if>
  </span>
  </#list>
<#-- TODO: Finish implementing -->
</#macro>
