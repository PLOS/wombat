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

<#include "./alterJournalTitle.ftl">

<meta name="citation_title" content="${article.title?replace('<.+?>',' ','r')?html}" />
<meta itemprop="name" content="${article.title?replace('<.+?>',' ','r')?html}" />
<#if article.journal??>
<meta name="citation_journal_title" content="${alterJournalTitle(article.journal)}" />
</#if>
<#if article.date??>
<meta name="citation_date" content="${article.date?date("yyyy-MM-dd")}" />
</#if>
<meta name="citation_firstpage" content="${article.eLocationId!}" />
<meta name="citation_issue" content="${article.issue}" />
<meta name="citation_volume" content="${article.volume}" />
<meta name="citation_issn" content="${article.eIssn}" />
<#if article.journal??>
<meta name="citation_journal_abbrev" content="${alterJournalTitle(article.journal)}" />
</#if>
<#if article.publisherName??>
<meta name="citation_publisher" content="${article.publisherName}" />
</#if>
<meta name="citation_pdf_url" content="${pubUrlPrefix}article/asset?id=${article.articlePdf.file}">

<#--//crossmark identifier-->
<meta name="dc.identifier" content="${articleDoi}" />

<#if pubUrlPrefix?has_content>
<link rel="canonical" href="${pubUrlPrefix}article?id=${articleDoi}" />
</#if>

<#if article.description??>
  <#if twitterUsername?has_content>
  <meta name="twitter:card" content="summary" />
  <meta name="twitter:site" content="${twitterUsername}"/>
  </#if>
<meta property="og:type" content="article" />
  <#if pubUrlPrefix?has_content>
  <meta property="og:url" content="${pubUrlPrefix}article?id=${articleDoi}" />
  </#if>
<meta property="og:title" content="${article.title?replace('<.+?>',' ','r')?html}" />
<meta property="og:description" content="${article.description?replace('<.+?>',' ','r')?html}" />
  <#if (article.strkImgURI?? && (article.strkImgURI?length > 0)) >
  <meta property="og:image" content="http://dx.plos.org/${article.strkImgURI?replace('info:doi/','')}" />
  </#if>
</#if>

<#--All of this data must be HTML char stripped to compensate for some XML in the database. If not, an ending tag can
 break out of the head and input all of the citation data directly into the visible dom. To further optimize,
 consider using a macro or function instead of the regex replace used below, or try to clean the data that's returned
 on the rhino side.-->
<#if article.citedArticles??>
  <#list article.citedArticles as citedArticle>
    <#if citedArticle.title??>
    <meta name="citation_reference" content="
      <#if citedArticle.title??>citation_title=${citedArticle.title?replace('<.+?>',' ','r')?html};</#if><#if citedArticle.authors?has_content>
      <#list citedArticle.authors as author>citation_author=${author.fullName?replace('<.+?>',' ','r')?html};</#list></#if><#if citedArticle.editors?has_content>
      citation_editors=<#list citedArticle.editors as editor>${editor.fullName?replace('<.+?>',' ','r')?html};</#list></#if><#if citedArticle.journal??>
      citation_journal_title=${citedArticle.journal?replace('<.+?>',' ','r')?html};</#if><#if citedArticle.volume??>
      citation_volume=${citedArticle.volume?replace('<.+?>',' ','r')?html};</#if><#if citedArticle.volumeNumber??>
      citation_number=${citedArticle.volumeNumber};</#if><#if citedArticle.pages??>
      citation_pages=${citedArticle.pages?replace('<.+?>',' ','r')?html};</#if><#if citedArticle.year??>
      citation_date=${citedArticle.year?string.computer};</#if>" />
    </#if>
  </#list>
</#if>
