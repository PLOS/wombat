<h1>Table of Contents: ${issue.displayName} ${issue.parentVolume.displayName}</h1>

<nav id="nav-toc">
  <ul class="nav-page">
    <li data-toc="Cover"><a href="#Cover">Cover</a></li>
  <#list articleGroups as articleGroup>
    <#if (articleGroup.articles?size > 1)>
      <#assign articleHeader="${articleGroup.type.pluralName!articleGroup.type.name!'No Header Defined'}">
    <#else>
      <#assign articleHeader="${articleGroup.type.name!'No Header Defined'}">
    </#if>
    <li data-toc="${articleGroup.type.name?replace(" ", "_")}"><a
        href="#${articleGroup.type.name?replace(" ", "_")}">${articleHeader}</a></li>
  </#list>
  </ul>
</nav>

<article>

<#if issue.imageArticle??>
  <div class="section cover">
    <a id="Cover" name="Cover" toc="Cover" title="Cover"></a>

    <div class="header">
      <p class="kicker">COVER</p>
      <@siteLink handlerName="browseIssues" queryParameters={"id": issue.doi}; issueLink>
        <h2><a href="${issueLink}">${issueTitle}</a></h2>
      </@siteLink>
    </div>
    <div class="detail-container">
      <div class="img">
        <a href="<@siteLink handlerName="article" queryParameters={"id": issue.imageArticle.doi} />">
          <img
              src="<@siteLink handlerName="assetFile" queryParameters={"type": "inline", "id": issue.imageArticle.figureImageDoi} />"
              alt="Issue Image" data-doi="${issue.imageArticle.doi}">
        </a>
      </div>
      <div class="txt">${issueDescription}</div>
    </div>
  </div>
</#if>


<#list articleGroups as articleGroup>
  <div class="section">
    <a id="${articleGroup.type.name?replace(" ", "_")}" name="${articleGroup.type.name?replace(" ", "_")}"
       toc="${articleGroup.type.name}" title="${articleGroup.type.name}"></a>
    <#if (articleGroup.articles?size > 1)>
      <#assign articleHeader="${articleGroup.type.pluralName!articleGroup.type.name!'No Header Defined'}">
    <#else>
      <#assign articleHeader="${articleGroup.type.name!'No Header Defined'}">
    </#if>

    <h2>${articleHeader!"No Header Defined"}</h2>
    <#list articleGroup.articles as articleInfo>
      <div class="item cf">
        <@siteLink handlerName="article" queryParameters={"id": articleInfo.doi} ; articleLink>
          <h3>
            <a href="${articleLink}" title="Read Open Access Article">
              <@xform xml=articleInfo.title/>
            </a>
          </h3>
        </@siteLink>

        <p class="authors">
          <#if articleInfo.authors??><#-- TODO: Support this -->
            <#list articleInfo.authors as auth>
              <#rt>${auth.fullName?trim}<#if auth_has_next>,</#if>
            </#list>
            <#if (articleInfo.collaborativeAuthors??)>
              <#if (articleInfo.authors?size > 0) && (articleInfo.collaborativeAuthors?size > 0)><#lt>,</#if>
              <#list articleInfo.collaborativeAuthors as cauth>
              ${cauth?trim}<#if cauth_has_next>,</#if>
              </#list>
            </#if>
          </#if>
        </p>


        <p class="article-info"><b>${journal.title}:</b> published
          <@formatJsonDate date="${articleInfo.publicationDate}" format="MMMM d, yyyy" /> |
          <#include "../macro/doiAsLink.ftl" />
          <@doiAsLink articleInfo.doi />
        </p>

        <#if articleGroup.type.name == "Research Article" >
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

        <#if articleInfo.relatedArticles??><#-- TODO: Support this -->
          <#if (articleInfo.relatedArticles?size > 0)>
            <h4>Related Articles</h4>
            <ol>
              <#list articleInfo.relatedArticles as relArticle>
                <li>
                  <a href="${doiResolverLink(relArticle.doi)}" title="Read Open Access Article">
                    <@xform xml=relArticle.title />
                  </a>
                </li>
              </#list>
            </ol>
          </#if>
        </#if>
      </div>
    </#list>
  </div>
</#list>
</article>
