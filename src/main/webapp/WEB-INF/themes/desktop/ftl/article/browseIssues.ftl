<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#assign cssFile="browse.css"/>
<#include "../macro/removeTags.ftl" />
<#include "../common/title/titleFormat.ftl" />
<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<!-- TODO: This is how Ambra does it, but it would be much preferred to provide the proper thumbnail asset file ids
      for each issue in the controller layer. -->
<#assign issue_image_suffix = ".g001.PNG_M"/>

<div id="toc-block">
    <h1>Table of Contents: ${issue.displayName} ${issue.parentVolume.displayName}</h1>

    <div class="layout-160_755 cf">

        <div class="col-1">
            <div class="nav" id="nav-toc">
                <ul>
                    <li id=cover">Cover</li>
                    <#list articleGroups as articleGrp>
                        <#if (articleGrp?size > 1)>
                            <#assign articleHeader="${articleGrp.pluralHeading!articleGrp.heading!'No Header Defined'}">
                        <#else>
                            <#assign articleHeader="${articleGrp.heading!'No Header Defined'}">
                        </#if>
                        <li id="${articleHeader}">${articleHeader}</li>
                    </#list>
                </ul>
            </div>
        </div>
        <!-- col-1 -->

        <div class="col-2">

            <div class="section cover cf">
                <a id="cover" name="cover" toc="cover" title="Cover"></a>

                <div class="header">
                    <div class="kicker">COVER</div>
                        <@siteLink
                        handlerName="browseIssues"
                        queryParameters={"id": issue.issueUri}; issueLink>
                            <h2><a href="${issueLink}">${issueTitle}</a></h2>
                        </@siteLink>
                    <div class="credit">Image Credit: ${issueImageCredit}</div>
                </div>
                <div class="img">
                <#if issue.imageUri?has_content>
                    <#assign issueImageFileId = issue.imageUri + issue_image_suffix/>
                    <img src="<@siteLink handlerName="asset" queryParameters={"id": issueImageFileId}/>"
                    alt="Issue Image" data-doi="${issue.imageUri}">
                </#if>
                </div>
                <div class="txt">${issueDescription}</div>
            </div>

        <#list articleGroups as articleGrp>
            <div class="section">
                <a id="${articleGrp.heading}" name="${articleGrp.heading}" toc="${articleGrp.heading}" title="${articleGrp.heading}"></a>
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

                            <div class="header">
                                <h3><a href="${articleLink}" title="Read Open Access Article">
                                    <@titleFormat removeTags(articleInfo.title) /></a>
                                </h3>

                                <div class="authors">
                                    <#list articleInfo.authors as auth>
                                    ${auth.fullName?trim}<#if auth_has_next>,</#if>
                                    </#list>
                                    <#if (articleInfo.collaborativeAuthors??)>
                                        <#if (articleInfo.authors?size > 0) && (articleInfo.collaborativeAuthors?size > 0)>,</#if>
                                        <#list articleInfo.collaborativeAuthors as cauth>
                                        ${cauth.fullName?trim}<#if cauth_has_next>,</#if>
                                        </#list>
                                    </#if>
                                </div>
                            </div>

                        </@siteLink>


                        <div class="article-info">
                            <p><b>${journal.title}:</b> published
                                <@formatJsonDate date="${articleInfo.date}" format="MMMM d, yyyy" /> | ${articleInfo.doi}</p>
                        </div>

                        <#if articleGrp.heading == "Research Article" >
                            <div class="links">
                            <#--assuming that all research articles have abstract-->
                                <a data-doi="${articleInfo.doi}" class="abstract">Abstract</a>
                                <#if (articleInfo.figures?size > 0)>
                                    &bull; <a data-doi="${articleInfo.doi}" class="figures">Figures</a>
                                </#if>
                            </div>
                        </#if>

                        <#if (articleInfo.relatedArticles?size > 0)>
                        <h4>Related Articles</h4>
                        <ul>
                            <#list articleInfo.relatedArticles as relArticle>
                                <li>
                                  <a href="http://dx.plos.org/${relArticle.doi?replace('info:doi/','')}"
                                     title="Read Open Access Article">>
                                      <@titleFormat removeTags(relArticle.title) />
                                  </a>
                                </li>
                            </#list>
                        </ul>
                        </#if>
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
