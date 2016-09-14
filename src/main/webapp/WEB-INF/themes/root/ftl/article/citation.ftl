<#--
    Prints a text block containing a citation for an article.

    Parameters:
      citation - The article to be cited. Must be an object containing a subset of the normal article
                 metadata fields.
      useUrlDoiStyle - Boolean. True to format the DOI as a URL at http://dx.doi.org/; false to
                       just prefix it with "doi:".
  -->
<#macro displayCitation citation useUrlDoiStyle authors=citation.authors>
  <#assign maxAuthors = 5 /><#-- May want this to be configurable in the future. -->

  <#list authors as author>
    <#if author_index lt maxAuthors>
      <#assign isCommaShown =
      author_has_next ||
      (authors?size lt maxAuthors         <#-- Show a comma before "et al."     -->
      && citation.collaborativeAuthors?size gt 0)  <#-- or if collab authors will follow -->
      />
    ${author.surnames!}
      <#if author.givenNames?has_content><@abbreviatedName>${author.givenNames}</@abbreviatedName></#if><#t/>
      <#if author.suffix?has_content> <#--space--> ${author.suffix?replace('.', '')}</#if><#t/>
      <#if isCommaShown><#t/>,</#if>
    </#if>
  </#list>
  <#assign maxCollabAuthors = maxAuthors - authors?size />
  <#list citation.collaborativeAuthors as author>
    <#if author_index lt maxCollabAuthors>
      <#assign isCommaShown = author_has_next || citation.collaborativeAuthors?size gt maxCollabAuthors />
    ${author}<#if isCommaShown>,</#if>
    </#if>
  </#list>

  <#if authors?size + citation.collaborativeAuthors?size gt maxAuthors>
  et al.
  </#if>

  <#if citation.publicationDate??>(<@formatJsonDate date="${citation.publicationDate}" format="yyyy" />)</#if>

  <#if citation.title??>
    <@xform xml=citation.title/><#if !citation.title?ends_with('?')>.</#if>
  </#if>

  <#if citation.journal??>
  ${citation.journal.title}<#t/>
    <#if citation.volume??> <#--space--> ${citation.volume}</#if><#if citation.issue??>(${citation.issue})</#if><#t/>
    <#if citation.eLocationId??><#t/>: ${citation.eLocationId}</#if>.
  </#if>

  <#if citation.doi??>
    <#if useUrlDoiStyle>
    http://dx.doi.org/${citation.doi}
    <#else>
    doi: ${citation.doi}
    </#if>
  </#if>

</#macro>
