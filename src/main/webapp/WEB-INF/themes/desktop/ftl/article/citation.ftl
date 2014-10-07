<#macro displayCitation citation>
<span class="citation">
<span class="authorList">
<#assign maxAuthors = 5 /><#-- May want this to be configurable in the future. -->

  <#list citation.authors as author>
    <#if author_index lt maxAuthors>
      <span class="citationAuthor">
      <#assign isCommaShown =
      author_has_next ||
      (citation.authors?size lt maxAuthors         <#-- Show a comma before "et al."     -->
      && citation.collaborativeAuthors?size gt 0)  <#-- or if collab authors will follow -->
      />
      ${author.surnames!}
        <#if author.givenNames?has_content><@abbreviatedName>${author.givenNames}</@abbreviatedName></#if><#t/>
        <#if author.suffix?has_content> <#--space--> ${author.suffix?replace('.', '')}</#if><#t/>
        <#if isCommaShown><#t/>,</#if>
    </span>
    </#if>
  </#list>

  <#assign maxCollabAuthors = maxAuthors - citation.authors?size />
  <#list citation.collaborativeAuthors as author>
    <#if author_index lt maxCollabAuthors>
      <span class="citationAuthor">
      <#assign isCommaShown = author_has_next || collaborativeAuthors?size lt maxCollabAuthors />
      ${author}<#if isCommaShown>,</#if>
    </span>
    </#if>
  </#list>

  <#if citation.authors?size + citation.collaborativeAuthors?size gt maxAuthors>
    et al.
  </#if>
</span>

  <#if citation.year??>
    <span class="citation_date">(${citation.year})</span>
  </#if>
  <#if citation.title??>
    <span class="citation_article_title">
    ${citation.title}<#if !citation.title?ends_with('?')>.</#if>
    </span>
  </#if>
  <#if citation.journal??>
    <span class="citation_journal_title">${citation.journal}</span>
  </#if>
  <span class="citation_issue">
  ${citation.volume!}<#if citation.issue??>(${citation.issue})</#if>:
  </span>
  <#if citation.eLocationId??>
    <span class="citation_start_page">${citation.eLocationId}.</span>
  </#if>

<#-- TODO: Add DOI -->

</span>
</#macro>
