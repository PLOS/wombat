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
    <#-- Show a comma if another author or "et al." will follow -->
      <#assign isCommaShown = author_has_next || authors?size gt maxAuthors />
    ${author.surnames!}
      <#if author.givenNames?has_content><@abbreviatedName>${author.givenNames}</@abbreviatedName></#if><#t/>
      <#if author.suffix?has_content> <#--space--> ${author.suffix?replace('.', '')}</#if><#t/>
      <#if isCommaShown><#t/>,</#if>
    </#if>
  </#list>

  <#if authors?size gt maxAuthors>
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
