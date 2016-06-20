<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#setting url_escaping_charset="UTF-8">
<#assign title = subjectName />
<#assign cssFile="browse-subject-area.css"/>

<#assign adPage="SubjectAreaLandingPage"/>

<#include "../../common/head.ftl" />
<#include "../../common/journalStyle.ftl" />
<#include "../../macro/searchResultsAlm.ftl" />



<body class="home ${journalStyle}">
<#include "../../common/header/headerContainer.ftl" />

<#function encodeSubject subject>
    <#return subject?replace(' ','_')?lower_case>
</#function>

<#if parameterMap["resultView"]??>
    <#assign resultView = parameterMap["resultView"]?first>
<#else>
    <#assign resultView = "cover">
</#if>
<@siteLink handlerName="browse" ; url>
    <#assign rootBrowseUrl = url/>
</@siteLink>
<#if subject?has_content>
  <@siteLink handlerName="browseSubjectArea" pathVariables={"subject":encodeSubject(subject)}; url>
    <#assign categoryBrowseUrl = url/>
  </@siteLink>
<#else>
  <#assign categoryBrowseUrl = rootBrowseUrl/>
</#if>
<div id="search-results-block" class="cf subject-listing">
    <div class="filter-bar subject cf">
        <h1>${subjectName}</h1>
        <ul>
            <li class="first">
                <a href="#" title="Related content">Related content</a><span></span>
            <#-- This xmlns mark up is for search engines to understand the hiearchy a bit -->
                <div xmlns:v="http://rdf.data-vocabulary.org/#" class="dropdown">
                    <ul typeof="v:Breadcrumb">
                    <#if subjectParents?? && subjectParents?size gt 0>
                      <#list subjectParents as parent>
                          <li><a rel="v:url" property="v:title" href="<@siteLink handlerName="browseSubjectArea" pathVariables={"subject": encodeSubject(parent)} />">${parent}</a></li>
                      </#list>
                    <#elseif subject?has_content>
                      <li><a rel="v:url" property="v:title" href="${rootBrowseUrl}">All Subject Areas</a></li>
                    </#if>
                      <li class="here" rel="v:child">
                            <a typeof="v:Breadcrumb" rel="v:url" href="${categoryBrowseUrl}"><div property="v:title" >${subjectName}</div><span></span></a>
                        <ul>
                        <#if subjectChildren??>
                          <#list subjectChildren as child>
                            <li><a href="<@siteLink handlerName="browseSubjectArea" pathVariables={"subject": encodeSubject(child)} />">${child}</a></li>
                          </#list>
                        </#if>
                        </ul>
                      </li>
                    </ul>
                </div><!-- /.dropdown -->
            </li>
        <#include "browseSubjectAreaAlert.ftl" />
        <#include "browseSubjectAreaRssFeed.ftl" />
        </ul>
    </div><!-- /.filter-bar -->

    <div class="header hdr-results subject cf">
        <div class="main">
        <#assign totalPages = ((searchResults.numFound + selectedResultsPerPage - 1) / selectedResultsPerPage)?int>
          <p class="count">
            Showing ${((page?number - 1) * selectedResultsPerPage) + 1} -
          <#if (searchResults.numFound lt selectedResultsPerPage)>
          ${searchResults.numFound}
          <#else>
            <#if ((page?number * selectedResultsPerPage) gt searchResults.numFound)>
            ${searchResults.numFound}
            <#else>
            ${page?number * selectedResultsPerPage}
            </#if>
          </#if>
            of ${searchResults.numFound}
          </p>

          <p class="sort">
              <span>View by:</span>
              <a id="cover-page-link" title="Cover page view" href="?<@replaceParams parameterMap=parameterMap replacements={"resultView": "cover"} />" class="<#if resultView == "cover">active</#if>">Cover Page</a>
              <a id="list-page-link" title="List page view" href="?<@replaceParams parameterMap=parameterMap replacements={"resultView": "list"} />" class="<#if resultView == "list">active</#if>">List Articles</a>
          </p>
        </div><!-- /.main -->
        <div class="sidebar">
            <p class="sort">
                <span>Sort by:</span>
            <#if selectedSortOrder == "DATE_NEWEST_FIRST">
                <span class="active">Recent</span>
                <a title="Sort by most viewed, all time" href="?<@replaceParams parameterMap=parameterMap replacements={"sortOrder": "MOST_VIEWS_ALL_TIME", "page": 1} />">Popular</a>
            <#else>
                <a title="Sort by most recent" href="?<@replaceParams parameterMap=parameterMap replacements={"sortOrder": "DATE_NEWEST_FIRST", "page": 1} />" >Recent</a>
                <span class="active">Popular</span>
            </#if>
            </p>
        </div><!-- /.sidebar -->
    </div><!-- /.hdr-results -->

    <#-- cover view -->

    <div id="subject-cover-view" class="subject-cover">
        <#if resultView == "list">
            <div id="subject-list-view" class="main">
                <#list articles as article>
                    <ul id="search-results">
                        <@siteLink handlerName="article" queryParameters={"id":article.doi} ; articleUrl>
                            <li data-doi="${article.doi}" data-pdate="${article.date}" data-metricsurl="<@siteLink handlerName="articleMetrics" />">
                                <h2><a href="${articleUrl}" title="${article.title}" class="list-title">${article.title}</a></h2>
                                <p class="authors">
                                    <#list article.authors as author>
                                        <span class="author">${author.fullName}<#if author_has_next>,</#if></span>
                                    </#list>
                                </p>
                                <p class="date"> published ${article.date?date("yyyy-MM-dd")?string("dd MMM yyyy")}</p>
                                <@searchResultsAlm article.doi/>
                                <p class="actions">
                                <#-- TODO: When able to launch lightbox from here, uncomment, remove alerts, and redirect to lightbox
                                    <a data-doi="info:doi/${article.doi}" class="abstract" href="#">Abstract</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <#if article.hasFigures>
                                        <a data-doi="info:doi/${article.doi}" class="figures" href="#" onclick="alert('Should redirect to lightbox.')">Figures</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <#else>
                                        <span class="disabled">Figures</span> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    </#if>
                                    <a href="${articleUrl}">Full Text</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <a href="<@siteLink handlerName="asset" queryParameters={"id": article.doi + ".PDF"} />" target="_blank">Download PDF</a>
                                -->
                                </p>
                            </li>
                        </@siteLink>
                    </ul>
                </#list>
            </div><!--main -->
        <#else>
            <div class="articles-list cf" data-subst="article-list">
            <#list articles as article>
                <#include "../../home/articleCard.ftl" />
            </#list>
            </div>
        </#if>

        <#include "../../home/twitter.ftl" />
        <#include "../../home/adSlotAside.ftl" />
        <#include "../../home/socialLinks.ftl" />
    </div>

</div>

<#include "../../common/paging.ftl" />
<#if subject?has_content>
  <#assign pagingPath = categoryBrowseUrl />
<#else>
  <#assign pagingPath = rootBrowseUrl />
</#if>
<@paging totalPages, page?number, pagingPath, parameterMap, true />

<#include "../../common/footer/footer.ftl" />

<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js" ></script>

<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js" />

<#include "subjectAreaJs.ftl" />

<@js src="resource/js/util/alm_config.js" />
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/components/search_results_alm.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>
<@js src="resource/js/vendor/jquery.dotdotdot.js" />
<@js src="resource/js/components/browse_results.js" />
<@renderJs />


<div id="login-box" class="login inlinePopup">
<#include "loginJournalAlertPopup.ftl"/>
</div>
<div id="journal-alert-box" class="journalAlert inlinePopup">
<#include "saveJournalAlertPopup.ftl"/>
</div>

</body>
</html>