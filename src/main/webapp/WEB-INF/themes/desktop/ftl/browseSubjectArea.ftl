<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#setting url_escaping_charset="UTF-8">
<#if filterSubjects?has_content>
    <#assign category = filterSubjects?first?cap_first!"" />
</#if>
<#assign title = category!"All Subject Areas" />
<#assign cssFile="browse-subject-area.css"/>
<#include "common/head.ftl" />
<#include "common/journalStyle.ftl" />

<body class="home ${journalStyle}">
<#include "common/header/headerContainer.ftl" />

<#function encodeSubject subject>
    <#return subject?replace(' ','_')?lower_case>
</#function>


<#if parameterMap["resultView"]??>
    <#assign resultView = parameterMap["resultView"]?first>
<#else>
    <#assign resultView = "cover">
</#if>
<@siteLink handlerName="browse" ; url>
    <#assign browseUrl = url/>
</@siteLink>
<#if category??>
  <@siteLink handlerName="browseSubjectArea" pathVariables={"subject":encodeSubject(category)}; url>
    <#assign fullBrowseUrl = url/>
  </@siteLink>
</#if>
<div id="search-results-block" class="cf subject-listing">
    <div class="filter-bar subject cf">
        <h1>${category!"All Subject Areas"}</h1>
        <ul>
            <li class="first">
                <a href="${browseUrl}" title="Related content">Related content</a><span></span>
            <#-- This xmlns mark up is for search engines to understand the hiearchy a bit -->
                <div xmlns:v="http://rdf.data-vocabulary.org/#" class="dropdown">
                    <ul typeof="v:Breadcrumb">
                    <#if parents?? && parents?size gt 0>
                      <#list parents as parent>
                          <li><a rel="v:url" property="v:title" href="<@siteLink handlerName="browseSubjectArea" pathVariables={"subject": encodeSubject(parent)} />"></a></li>
                        <#-- Only mark up the first element as part of the breadcrumb parent -->
                          <#if parent_index == 0>
                              <li><a rel="v:url" property="v:title" href="${browseUrl}">${parent}</a></li>
                          <#else>
                              <li><a href="./">${parent}</a></li>
                          </#if>
                      </#list>
                    <#else>
                      <#if category??>
                        <li><a rel="v:url" property="v:title" href="${browseUrl}">All Subject Areas</a></li>
                      </#if>
                    </#if>
                      <li class="here" rel="v:child">
                            <#--<a typeof="v:Breadcrumb" rel="v:url" href="${fullBrowseUrl}"><div property="v:title" >${category!"All Subject Areas"}</div><span></span></a>-->
                        <ul>
                        <#if children??>
                          <#list children as child>
                            <li><a href="<@siteLink handlerName="browseSubjectArea" pathVariables={"subject": encodeSubject(child)} />">${child}</a></li>
                          </#list>
                        </#if>
                        </ul>
                      </li>
                    </ul>
                </div><!-- /.dropdown -->
            </li>
        <#--@TODO: Handle session and subscription-->
        <#--
            <#if category??>
                <li class="middle">
                                 <#if Session?exists && Session[freemarker_config.userAttributeKey]?exists>
                    <#if subscribed>
                      <#assign subscribedClass = " subscribed">
                    <#else>
                      <#assign subscribedClass = "">
                    </#if>
                      <a href="#" title="Get an email alert for ${category}" class="journal-alert${subscribedClass}" id="save-journal-alert-link" data-category="${category}">Get an email alert for ${category}</a>
                  <#else>
                      <a href="#" title="Get an email alert for ${category}" id="login-link" data-category="${category}">Get an email alert for ${category}</a>
                  </#if>
                </li>
            </#if>
            -->
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
              <a id="cover-page-link" title="Cover page view" href="?<@replaceParams parameterMap=parameterMap name="resultView" value="cover" />" class="<#if resultView == "cover">active</#if>">Cover Page</a>
              <a id="list-page-link" title="List page view" href="?<@replaceParams parameterMap=parameterMap name="resultView" value="list" />" class="<#if resultView == "list">active</#if>">List Articles</a>
          </p>
        </div><!-- /.main -->
        <div class="sidebar">
            <p class="sort">
                <span>Sort by:</span>
            <#if selectedSortOrder == "DATE_NEWEST_FIRST">
                <span class="active">Recent</span>
                <a title="Sort by most viewed, all time" href="?<@replaceParams parameterMap=parameterMap name="sortOrder" value="MOST_VIEWS_ALL_TIME" />">Popular</a>
            <#else>
                <a title="Sort by most recent" href="?<@replaceParams parameterMap=parameterMap name="sortOrder" value="DATE_NEWEST_FIRST" />" >Recent</a>
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
                            <#include "article/articleTruncateTitle.ftl" />
                            <li data-doi="${article.doi}" data-pdate="${article.date}" data-metricsurl="<@siteLink handlerName="articleMetrics" />">
                                <h2><a href="${articleUrl}" title="${article.title}"><@truncateTitle article.title></@></a></h2>
                                <p class="authors">
                                    <#list article.authors as author>
                                        <span class="author">${author.fullName}<#if author_has_next>,</#if></span>
                                    </#list>
                                </p>
                                <p class="date">published ${article.date?date("yyyy-mm-dd")?string("dd MMM yyyy")}</p>
                                <span class="metrics"><span>Loading metrics information...</span></span>
                                <p class="actions">
                                    <a data-doi="info:doi/${article.doi}" class="abstract" href="#">Abstract</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <#if article.hasFigures>
                                        <a data-doi="info:doi/${article.doi}" class="figures" href="#" onclick="alert('Should redirect to lightbox.')">Figures</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <#else>
                                        <span class="disabled">Figures</span> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    </#if>
                                    <a href="${articleUrl}">Full Text</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <a href="<@siteLink handlerName="asset" queryParameters={"id": article.doi + ".PDF"} />" target="_blank">Download PDF</a>
                                </p>
                            </li>
                        </@siteLink>
                    </ul>
                </#list>
            </div><!--main -->
        <#else>
            <div class="articles-list cf" data-subst="article-list">
            <#list articles as article>
                <#include "home/articleCard.ftl" />
            </#list>
            </div>
        </#if>

        <#include "home/twitter.ftl" />
        <#include "home/adSlotAside.ftl" />
        <#include "home/socialLinks.ftl" />
    </div>

</div>

<#include "common/paging.ftl" />
<@paging totalPages, page?number, (category??)?string(fullBrowseUrl, browseUrl), parameterMap />

<#include "common/footer/footer.ftl" />

<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js" ></script>

<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js" />

<#include "subjectAreaJs.ftl" />

<@js src="resource/js/util/alm_config.js" />
<@js src="resource/js/metrics.js" />
<@js src="resource/js/components/tooltip_hover.js"/>
<@js src="resource/js/components/browse_results.js" />
<@renderJs />

</body>
</html>