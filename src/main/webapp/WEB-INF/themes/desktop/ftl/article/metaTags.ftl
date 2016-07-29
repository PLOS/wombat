<#include "../common/twitterConfig.ftl" />
<#include "../common/pubUrlPrefix.ftl" />
<#include "../macro/doiResolverLink.ftl" />

<#--//analytics related meta tags - description and keywords-->
<#--todo: these assignments should be moved to the controller-->
<#--todo: stop repeating "replace('<.+?>',' ','r')"-->
<#if article.description??>
  <#assign articleDescription=article.description?replace('<.+?>',' ','r')?html/>
  <meta name="description" content="${articleDescription}" />
</#if>
<#if article.title??>
  <#assign articleTitle=article.title?replace('<.+?>',' ','r')?html/>
</#if>

<#if categoryTerms??>
  <meta name="keywords" content="<#list categoryTerms as categoryTerm>${categoryTerm}<#if categoryTerm_has_next>,</#if></#list>" />
</#if>

<meta name="citation_doi" content="${article.doi}" />
<#if article.authors?? >
  <#list authors as author>
  <meta name="citation_author" content="${author.fullName}" />
      <#list author.affiliations as affiliation>
        <meta name="citation_author_institution" content="${affiliation?trim}" />
      </#list>
  </#list>
</#if>
<#assign hasStrkImgUri=article.strkImgURI?? && (article.strkImgURI?length > 0) />

<#include "./alterJournalTitle.ftl">

<#if article.title??>
  <meta name="citation_title" content="${articleTitle}" />
  <meta itemprop="name" content="${articleTitle}" />
</#if>
<#if article.journal??>
  <meta name="citation_journal_title" content="${alterJournalTitle(article.journal)}" />
  <meta name="citation_journal_abbrev" content="${alterJournalTitle(article.journal)}" />
</#if>
<#if article.date??>
  <meta name="citation_date" content="${article.date?date("yyyy-MM-dd")}" />
</#if>
<meta name="citation_firstpage" content="${article.eLocationId!}" />
<meta name="citation_issue" content="${article.issue}" />
<meta name="citation_volume" content="${article.volume}" />
<meta name="citation_issn" content="${article.eIssn}" />

<#if article.publisherName??>
  <meta name="citation_publisher" content="${article.publisherName}" />
</#if>

<#if articleItems[article.doi].files?keys?seq_contains("printable")>
  <@siteLink handlerName="assetFile" queryParameters={"type": "printable", "id": article.doi} absoluteLink=true ; citationPdfUrl>
  <meta name="citation_pdf_url" content="${citationPdfUrl}">
  </@siteLink>
</#if>

<#--//crossmark identifier-->
<meta name="dc.identifier" content="${article.doi}" />

<#if twitterUsername?has_content>
  <meta name="twitter:card" content="summary" />
  <meta name="twitter:site" content="${twitterUsername}"/>
  <#if article.title??>
    <meta name="twitter:title" content="${articleTitle}" />
  </#if>
  <#if article.description??>
    <meta property="twitter:description" content="${articleDescription}" />
  </#if>
  <#if hasStrkImgUri >
    <@siteLink handlerName="figureImage" queryParameters={"id" : article.strkImgURI, "size": "inline"} ; href>
    <meta property="twitter:image" content="${href}" />
    </@siteLink>
  </#if>
</#if>

<meta property="og:type" content="article" />
<#if pubUrlPrefix?has_content>
  <meta property="og:url" content="${pubUrlPrefix}article?id=${article.doi}" />
</#if>
<#if article.title??>
  <meta property="og:title" content="${articleTitle}" />
</#if>
<#if article.description??>
  <meta property="og:description" content="${articleDescription}" />
</#if>
<#if hasStrkImgUri >
  <@siteLink handlerName="figureImage" queryParameters={"id" : article.strkImgURI, "size": "inline"} ; href>
  <meta property="og:image" content="${href}" />
  </@siteLink>
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
