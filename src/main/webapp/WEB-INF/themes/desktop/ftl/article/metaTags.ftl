<#include "../common/twitterConfig.ftl" />
<#include "../common/pubUrlPrefix.ftl" />

<meta name="citation_doi" content="${articleDoi}" />
<#if article.authors?? >
  <#list authors as author>
  <meta name="citation_author" content="${author.fullName}" />
      <#list author.affiliations as affiliation>
        <meta name="citation_author_institution" content="${affiliation?trim}" />
      </#list>
  </#list>
</#if>

<meta name="citation_title" content="${article.title?replace('<.+?>',' ','r')?html}"/>
<meta itemprop="name" content="${article.title?replace('<.+?>',' ','r')?html}"/>

<#if article.date??>
<meta name="citation_date" content="${article.date}"/>
</#if>

<#if article.citedArticles??>
  <#list article.citedArticles as citedArticle>
  <meta name="citation_reference"
        content="
        <#if citedArticle.title??>citation_title=${citedArticle.title};</#if>
        <#if citedArticle.authors??>citation_author=<#list citedArticle.authors as author>${author.fullName};</#list></#if>
        <#if citedArticle.journal??>citation_journal_title=${citedArticle.journal};</#if>
        <#if citedArticle.volume??>citation_volume=${citedArticle.volume};</#if>
        <#if citedArticle.volumeNumber??>citation_number=${citedArticle.volumeNumber};</#if>
        <#if citedArticle.pages??>citation_pages=${citedArticle.pages};</#if>
        <#if citedArticle.created??>citation_date=${citedArticle.created};</#if>" />
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
