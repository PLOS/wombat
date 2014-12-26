<#include "../common/twitterConfig.ftl" />
<#include "../common/pubUrlPrefix.ftl" />
<#-- need backend for these
<meta name="citation_publisher" content="${article.publisher}" />

  <#if article.unformattedTitle??>
  <meta name="citation_title" content="${article.unformattedTitle}"/>
  <meta itemprop="name" content="${article.unformattedTitle}"/>
  </#if>
  -->

<meta name="citation_doi" content="${articleDoi}" />
<#if article.authors?? >
  <#list authors as author>
  <meta name="citation_author" content="${author.fullName}" />
      <#list author.affiliations as affiliation>
        <meta name="citation_author_institution" content="${affiliation?trim}" />
      </#list>
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

<#if journalAbbrev??>
<meta name="citation_journal_abbrev" content="${journalAbbrev}" />
</#if>
<#--//crossmark identifier-->
<meta name="dc.identifier" content="${articleDoi}" />

<#if pubUrlPrefix?has_content>
<link rel="canonical" href="${pubUrlPrefix}article?id=${articleDoi}" />
</#if>

<#if article.description??>
  <#if twitterUsername?has_content>
    <meta name="twitter:card" content="summary"/>
    <meta name="twitter:site" content="${twitterUsername}"/>
  </#if>
  <meta property="og:type" content="article" />
  <#if pubUrlPrefix?has_content>
    <meta property="og:url" content="${pubUrlPrefix}article?id=${articleDoi}"/>
  </#if>
  <meta property="og:title" content="${article.title?replace('<.+?>',' ','r')?html}"/>
  <meta property="og:description" content="${article.description?replace('<.+?>',' ','r')?html}"/>
  <#if (article.strkImgURI?? && (article.strkImgURI?length > 0)) >
  <meta property="og:image" content="http://dx.plos.org/${article.strkImgURI?replace('info:doi/','')}"/>
  </#if>
</#if>
