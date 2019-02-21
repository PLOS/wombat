<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<small class="item-title lead-in">Table of Contents</small>
<h2>${issue.displayName} ${issue.parentVolume.displayName}</h2>

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
      <small class="item-title lead-in">Cover</small>
      <@siteLink handlerName="browseIssues" queryParameters={"id": issue.doi}; issueLink>
        <a href="${issueLink}">${issueTitle}</a>
      </@siteLink>

    </div>
    <div class="detail-container">
      <div class="img">
        <a href="<@siteLink handlerName="article" queryParameters={"id": issue.imageArticle.doi} />">
          <img
              src="<@siteLink handlerName="figureImage" queryParameters={"size": "inline", "id": issue.imageArticle.figureImageDoi} />"
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

    <small class="item-title lead-in">${articleHeader!"No Header Defined"}</small>
    <#list articleGroup.articles as articleInfo>
      <div class="item cf">
        <@siteLink handlerName="article" queryParameters={"id": articleInfo.doi} ; articleLink>
          <h4 class="item--article-title"><b>
            <a href="${articleLink}" title="Read Open Access Article">
              <@xform xml=articleInfo.ingestion.title/>
            </a>
            </b>
          </h4>
        </@siteLink>

        <p class="authors">
          <#if articleInfo.authors??>
            <#list articleInfo.authors as auth>
              <#rt>${auth.fullName?trim}<#if auth_has_next>,</#if>
            </#list>
          </#if>
        </p>

        <p class="article-info"><i>${journal.title}</i>: published
          <@formatJsonDate date="${articleInfo.ingestion.publicationDate}" format="MMMM d, yyyy" /> |
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

        <#if articleInfo.relatedArticles??>
          <#if (articleInfo.relatedArticles?size > 0)>
          <dl>
            <dt><small class="item-title">Related Articles</small></dt>
            
              <#list articleInfo.relatedArticles as relArticle>
                <dd>
                  <a href="${doiResolverLink(relArticle.doi)}" title="Read Open Access Article">
                    <@xform xml=relArticle.title />
                  </a>
                </dd>
              </#list>
          </dl>
          </#if>
        </#if>
      </div>
    </#list>
  </div>
</#list>
</article>
