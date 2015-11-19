<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#assign cssFile="browse.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<div id="toc-block">
    <h1>Table of Contents: ${issue.displayName} ${issue.parentVolume.displayName}</h1>

    <div class="layout-160_755 cf">

        <div class="col-1">
            <div class="nav" id="nav-toc"></div>
        </div>
        <!-- col-1 -->

        <div class="col-2">

            <div class="section cover cf">
                <a id="cover" name="cover" toc="cover" title="Cover"></a>

                <div class="header">
                    <div class="kicker">COVER</div>
                <@s.url id="issueURL" action="issue" namespace="/article/browse" issue="${issueInfo.issueURI}"/>
                    <h2><a href="${issueURL}">${issueTitle}</a></h2>

                    <div class="credit">Image Credit: ${issueImageCredit}</div>
                </div>
                <div class="img">
                <#if issueInfo.imageArticle?has_content>
                    <@s.url id="imageSmURL" action="fetchObject" namespace="/article" uri="${issueInfo.imageArticle}.g001" representation="PNG_M" includeParams="none"/>
                    <img src="${imageSmURL}" alt="Issue Image" data-doi="${issueInfo.imageArticle}">
                </#if>
                </div>
                <div class="txt">${issueDescription}</div>
            </div>

        <#list articleGroups as articleGrp>
            <div class="section">
                <a id="${articleGrp.id}" name="${articleGrp.id}" toc="${articleGrp.id}" title="${articleGrp.heading}"></a>
                <#if (articleGrp.count > 1)>
                    <#assign articleHeader="${articleGrp.pluralHeading!articleGrp.heading!'No Header Defined'}">
                <#else>
                    <#assign articleHeader="${articleGrp.heading!'No Header Defined'}">
                </#if>
                <h2>${articleHeader!"No Header Defined"}</h2>
                <#list articleGrp.articles as articleInfo>
                    <div class="item cf">
                        <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.doi}"
                        includeParams="none"/>
                        <div class="header">
                            <h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">
                        <@articleFormat>${articleInfo.title}</@articleFormat></@s.a>
                            </h3>

                            <div class="authors">
                                <#list articleInfo.authors as auth>
                                ${auth?trim}<#if auth_has_next>,</#if>
                                </#list>
                                <#if (articleInfo.collaborativeAuthors??)>
                                    <#if (articleInfo.authors?size > 0) && (articleInfo.collaborativeAuthors?size > 0)>,</#if>
                                    <#list articleInfo.collaborativeAuthors as cauth>
                                    ${cauth?trim}<#if cauth_has_next>,</#if>
                                    </#list>
                                </#if>
                            </div>
                        </div>

                    <#--Don't have content for this section yet>
                      <div class="txt">
                      <p></p>
                    </div>-->

                        <div class="article-info">
                            <p><b>${articleInfo.publishedJournal}:</b> published ${articleInfo.date?date?string("dd MMM yyyy")} | ${articleInfo.doi}</p>
                        </div>

                        <#if (articleGrp.heading == "Research Article") >
                            <div class="links">
                            <#--assuming that all research articles have abstract-->
                                <a data-doi="${articleInfo.doi}" class="abstract">Abstract</a> &bull;
                                <@s.a href="%{fetchArticleURL}#abstract1" title="Read Author Summary">Author Summary</@s.a>
                                <#if (articleInfo.hasFigures)>
                                    &bull; <a data-doi="${articleInfo.doi}" class="figures">Figures</a>
                                </#if>
                            </div>
                        </#if>

                        <@related articleInfo=articleInfo/>
                    </div>
                </#list>
            </div>
        </#list>
        </div>
        <!-- col-2 -->

    </div>
    <!-- layout-625_300 -->
</div>
<!-- toc-block -->

<#include "../common/footer/footer.ftl" />

<@js src="resource/js/components/scroll.js"/>
<@js src="resource/js/components/floating_nav.js"/>
<@renderJs />

</body>
</html>
