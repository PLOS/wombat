<#-- need backend for these
<meta name="citation_publisher" content="${article.publisher}" />

  <#if article.unformattedTitle??>
  <meta name="citation_title" content="${article.unformattedTitle}"/>
  <meta itemprop="name" content="${article.unformattedTitle}"/>
  </#if>
  -->

<meta name="citation_doi" content="${article.doi?replace('info:doi/','')}" />
<#if article.authors?? >
  <#list authors as author>
  <meta name="citation_author" content="${author.fullName}" />
    <#if author.affiliations?? >
      <#list author.affiliations as affiliation>
        <#if affiliation?? >
        <meta name="citation_author_institution" content="${affiliation?trim}" />
        </#if>
      </#list>
    </#if>
  </#list>
</#if>

<#if article.date??>
<meta name="citation_date" content="${article.date}"/>
</#if>
<#if article.references??>
  <#list references as reference>
  <meta name="citation_reference" content="${reference.referenceContent}" />
  </#list>
</#if>
<#if article.publishedJournal??>
<meta name="citation_journal_title" content="${article.publishedJournal}" />
</#if>
<meta name="citation_firstpage" content="${article.eLocationId!}"/>
<meta name="citation_issue" content="${article.issue}"/>
<meta name="citation_volume" content="${article.volume}"/>
<meta name="citation_issn" content="${article.eIssn}"/>
<#include "../common/legacyLink.ftl" />
<#assign pdfURL = "${legacyUrlPrefix}article/fetchObject.action?uri=info:doi/${article.doi}&representation=PDF" />
<meta name="citation_pdf_url" content="${pdfURL}" />

<#if journalAbbrev??>
<meta name="citation_journal_abbrev" content="${journalAbbrev}" />
</#if>
<#--//crossmark identifier-->
<meta name="dc.identifier" content="${article.doi}" />

