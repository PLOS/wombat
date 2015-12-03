<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#setting url_escaping_charset="UTF-8">
<#assign category = filterSubjects?first?cap_first!"" />
<#assign title = category!"All Subject Areas" />
<#assign cssFile="plos-one-browse.css"/>
<#include "common/head.ftl" />
<body class="home">
<#include "common/header/headerContainer.ftl" />

<#assign resultView = RequestParameters.resultView!"cover">
<@siteLink handlerName="browse" ; url>
    <#assign browseUrl = url/>
    <#assign fullBrowseUrl = browseUrl+"/"+category?replace(' ','_')?lower_case?url/>
</@siteLink>
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
                            <li><a rel="v:url" property="v:title" href="./${parent?replace(' ','_')?lower_case?url}"><#--@TODO BROWSE URL--></a></li>
                        <#-- Only mark up the first element as part of the breadcrumb parent -->
                          <#if parent_index == 0>
                              <li><a rel="v:url" property="v:title" href="./">${parent}</a></li>
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
                            <a typeof="v:Breadcrumb" rel="v:url" href="${fullBrowseUrl}"><div property="v:title" >${category!"All Subject Areas"}</div><span></span></a>
                        <ul>
                        <#if children??>
                          <#list children as child>
                            <li><a href="./${child?replace(' ','_')?lower_case?url}">${child}</a></li>
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

    <#--    <#if category??>
          <@s.url id="feedURL" unformattedQuery="subject:\"${category}\"" sort = "${sort}" filterJournals = "${currentJournal}" namespace="/article/feed" action="executeFeedSearch" />
        <#else>
          <@s.url id="feedURL" unformattedQuery="*:*" sort = "${sort}" filterJournals = "${currentJournal}" namespace="/article/feed" action="executeFeedSearch" />
        </#if>
            <li class="last"><a href="${feedURL}" title="Get the RSS feed for ${category!"all articles"}">Get the RSS feed for ${category!"all articles"}</a></li>-->
        </ul>
    </div><!-- /.filter-bar -->

    <#--@TODO: Handle whitespace in macro -->
    <#macro URLParameters resultView="cover" sortOrder="DATE_NEWEST_FIRST" page="1" resultsPerPage="15">${"resultView="+resultView?url+"&"+"sortOrder="+sortOrder?url+"&"+"page="+page?url+"&"+"resultsPerPage="+resultsPerPage?url}</#macro>

    <div class="header hdr-results subject cf">
        <div class="main">
          <#assign totalPages = ((searchResults.numFound + selectedResultsPerPage - 1) / selectedResultsPerPage)?int>
          <p class="count">Showing ${(page?number * selectedResultsPerPage) + 1} - <#if (searchResults.numFound lt selectedResultsPerPage)>${searchResults.numFound}<#else>
            <#if (((page?number + 1) * selectedResultsPerPage) gt searchResults.numFound)>${searchResults.numFound}<#else>${(page?number + 1)* selectedResultsPerPage}</#if></#if> of ${searchResults.numFound}</p>
          <p class="sort">
              <span>View by:</span>
              <a id="cover-page-link" title="Cover page view" href="?<@URLParameters resultView="cover" sortOrder=sortOrder page=page/>" class="<#if resultView == "cover">active</#if>">Cover Page</a>
              <a id="list-page-link" title="List page view" href="?<@URLParameters resultView="list" sortOrder=sortOrder page=page />" class="<#if resultView == "list">active</#if>">List Articles</a>
          </p>
        </div><!-- /.main -->
        <div class="sidebar">
            <p class="sort">
                <span>Sort by:</span>
            <#if selectedSortOrder == "DATE_NEWEST_FIRST">
                <span class="active">Recent</span>
                <a title="Sort by most viewed, all time" href="?<@URLParameters resultView=resultView sortOrder="MOST_VIEWS_ALL_TIME" page=page />">Popular</a>
            <#else>
                <a title="Sort by most recent" href="?<@URLParameters resultView=resultView sortOrder="DATE_NEWEST_FIRST" page=page />" >Recent</a>
                <span class="active">Popular</span>
            </#if>
            </p>
        </div><!-- /.sidebar -->
    </div><!-- /.hdr-results -->

    <#-- cover view -->

    <div id="subject-cover-view" class="subject-cover">
        <#if resultView == "list">
            <div id="subject-list-view" class="main">
                <#list searchResults.docs as article>
                    <ul id="search-results">
                        <@siteLink handlerName="article" queryParameters={"id":article.id} ; articleUrl>
                            <#include "article/articleTruncateTitle.ftl" />
                            <li data-doi="${article.id}" data-pdate="${article.publication_date}" data-metricsurl="<@siteLink handlerName="articleMetrics" />">
                                <h2><a href="${articleUrl}" title="${article.title}"><@truncateTitle article.title></@></a></h2>
                                <p class="authors">
                                    <#list article.author_display![] as author>
                                        <span class="author">${author}<#if author_has_next>,</#if></span>
                                    </#list>
                                </p>
                                <p class="date">published ${article.publication_date}</p> <#--?string("dd MMM yyyy")-->
                                <span class="metrics"><span>Loading metrics information...</span></span>
<#--                                <span class="metrics" style="display: block;">
                                    <span class="almSearchWidget">
                                        <span>
                                            <a href="<@siteLink handlerName="articleMetrics" queryParameters={"id":article.id} />#usage" class="data">
                                                Views: ${article.counter_total_all}
                                            </a>
                                        </span>&nbsp;•&nbsp;
                                        <span>
                                            <a href="<@siteLink handlerName="articleMetrics" queryParameters={"id":article.id} />#citations" class="data">
                                                Citations: ${article.alm_scopusCiteCount}
                                            </a>
                                        </span>&nbsp;•&nbsp;
                                        <span>
                                            <a href="<@siteLink handlerName="articleMetrics" queryParameters={"id":article.id} />#other" class="data">
                                                Saves: ${article.alm_mendeleyCount}
                                            </a>
                                        </span>&nbsp;•&nbsp;
                                        <span class="no-data">
                                            Shares: None
                                        </span>
                                    </span>
                                </span>-->
                                <p class="actions">
                                    <a data-doi="info:doi/${article.id}" class="abstract" href="#">Abstract</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <#--@TODO: Wait for article.hasAssets fix in BE -->
    <#--                                <#if (article.hasAssets == true) >
                                        <a data-doi="info:doi/${article.id}" class="figures" href="#">Figures</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <#else></#if> -->
                                    <span class="disabled">Figures</span> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <a href="${articleUrl}">Full Text</a> &nbsp;&nbsp;|&nbsp;&nbsp;
                                    <a href="<@siteLink handlerName="asset" queryParameters={"id": article.id + ".PDF"} />" target="_blank">Download PDF</a>
                                </p>
                            </li>
                        </@siteLink>
                    </ul>
                </#list>
            </div><!--main -->
        <#else>
            <div class="articles-list cf" data-subst="article-list">
                <#list searchResults.docs as article>
                <#include "home/articleCard.ftl" />
            </#list>
            </div>
        </#if>

        <#include "home/twitter.ftl" />
        <#include "article/aside/adSlotAside.ftl" />
        <#include "home/socialLinks.ftl" />
    </div>

</div>

<#include "renderSearchPaginationLinks.ftl" />
<@renderSearchPaginationLinks url=fullBrowseUrl totalPages=totalPages currentPage=page?number/>

<#include "common/footer/footer.ftl" />

<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js" ></script>

<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js" />

<@js src="resource/js/plosone.js" />
<@js src="resource/js/util/alm_config.js" />
<@js src="resource/js/metrics.js" />
<@js src="resource/js/components/tooltip_hover.js"/>
<@js src="resource/js/components/browse_results.js" />
<@renderJs />

</body>
</html>