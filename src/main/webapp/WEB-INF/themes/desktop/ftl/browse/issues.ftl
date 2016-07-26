<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign cssFile="browse-issue.css"/>
<#include "../common/title/issuesTitle.ftl" />
<#assign title = issuesTitle />

<#include "../macro/removeTags.ftl" />
<#include "../macro/doiResolverLink.ftl" />
<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="browse-issue ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<section >
    <h1>Table of Contents: ${issue.displayName} ${issue.parentVolume.displayName}</h1>

            <nav id="nav-toc" >
                <ul class="nav-page">
                    <li data-toc="Cover"><a href="#Cover">Cover</a></li>
                    <#list articleGroups as articleGrp>
                        <#if (articleGrp?size > 1)>
                            <#assign articleHeader="${articleGrp.pluralHeading!articleGrp.heading!'No Header Defined'}">
                        <#else>
                            <#assign articleHeader="${articleGrp.heading!'No Header Defined'}">
                        </#if>
                        <li data-toc="${articleGrp.heading?replace(" ", "_")}"><a href="#${articleGrp.heading?replace(" ", "_")}">${articleHeader}</a></li>
                    </#list>
                </ul>
            </nav>
        <!-- col-1 -->

        <article>

            <div class="section cover">
                <a id="Cover" name="Cover" toc="Cover" title="Cover"></a>

                <div class="header">
                    <p class="kicker">COVER</p>
                        <@siteLink
                        handlerName="browseIssues"
                        queryParameters={"id": issue.issueUri}; issueLink>
                            <h2><a href="${issueLink}">${issueTitle}</a></h2>
                        </@siteLink>
                </div>
              <div class="detail-container">
                <div class="img">
                <#if issue.imageUri?has_content>
                  <#assign issueImageFigureDoi = 'TODO' /><#-- TODO: Retrieve figure DOI from metadata -->
                  <a href="<@siteLink handlerName="article" queryParameters={"id": issue.imageUri} />">
                    <img src="<@siteLink handlerName="assetFile" queryParameters={"type": "inline", "id": issueImageFigureDoi} />"
                         alt="Issue Image" data-doi="${issue.imageUri}">
                  </a>
                </#if>
                </div>
                <div class="txt">${issueDescription}</div>
              </div>
            </div>

        <#list articleGroups as articleGrp>
            <div class="section">
                <a id="${articleGrp.heading?replace(" ", "_")}" name="${articleGrp.heading?replace(" ", "_")}" toc="${articleGrp.heading}" title="${articleGrp.heading}"></a>
                <#if (articleGrp?size > 1)>
                    <#assign articleHeader="${articleGrp.pluralHeading!articleGrp.heading!'No Header Defined'}">
                <#else>
                    <#assign articleHeader="${articleGrp.heading!'No Header Defined'}">
                </#if>

                <h2>${articleHeader!"No Header Defined"}</h2>
                <#list articleGrp.articles as articleInfo>
                    <div class="item cf">
                        <@siteLink
                        handlerName="article"
                        queryParameters={"id": articleInfo.doi}; articleLink>

                                <h3>
                                  <a href="${articleLink}" title="Read Open Access Article">
                                    ${articleInfo.title}
                                  </a>
                                </h3>

                                <p class="authors">
                                  <#list articleInfo.authors as auth>
                                    <#rt>${auth.fullName?trim}<#if auth_has_next>,</#if>
                                  </#list>
                                  <#if (articleInfo.collaborativeAuthors??)>
                                      <#if (articleInfo.authors?size > 0) && (articleInfo.collaborativeAuthors?size > 0)><#lt>,</#if>
                                      <#list articleInfo.collaborativeAuthors as cauth>
                                        ${cauth?trim}<#if cauth_has_next>,</#if>
                                      </#list>
                                  </#if>
                                </p>

                        </@siteLink>


                      <p class="article-info"><b>${journal.title}:</b> published
                        <@formatJsonDate date="${articleInfo.date}" format="MMMM d, yyyy" /> |
                        <#include "../macro/doiAsLink.ftl" />
                        <@doiAsLink articleInfo.doi />
                      </p>

                        <#if articleGrp.heading == "Research Article" >
                            <p class="links">
                            <#--assuming that all research articles have abstract-->
                            <#-- TODO: When able to launch lightbox from here, uncomment and wire to lightbox
                                <a data-doi="${articleInfo.doi}" class="abstract">Abstract</a>
                                <#if (articleInfo.figures?size > 0)>
                                    &bull; <a data-doi="${articleInfo.doi}" class="figures">Figures</a>
                                </#if>
                            -->
                            </p>
                        </#if>

                        <#if (articleInfo.relatedArticles?size > 0)>
                        <h4>Related Articles</h4>
                        <ol>
                            <#list articleInfo.relatedArticles as relArticle>
                                <li>
                                  <a href="${doiResolverLink(relArticle.doi)}"
                                     title="Read Open Access Article">
                                      ${relArticle.title}
                                  </a>
                                </li>
                            </#list>
                        </ol>
                        </#if>
                    </div>
                </#list>
            </div>
        </#list>
        </article>
        <!-- col-2 -->

</section>

<#include "../common/footer/footer.ftl" />

<@js src="resource/js/components/scroll.js"/>
<@js src="resource/js/components/floating_nav.js"/>
<@js src="resource/js/pages/browse-issues.js"/>

<@renderJs />

</body>
</html>
