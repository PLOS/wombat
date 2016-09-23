<header class="title-block">
<#include "signposts.ftl" />
    <div class="article-meta">
    <#include "articleClassifications.ftl" />
    </div>
    <div class="article-title-etc">

    <#include "articleTitle.ftl" />

      <ul class="date-doi">
      <#if revisionMenu.revisions?size gt 1>
          <li class="revisionList">
            <#include "revision/revisionMenu.ftl" />
          </li>
        </#if>
        <li id="artPubDate">Published: <@formatJsonDate date="${article.publicationDate}" format="MMMM d, yyyy" /></li>
        <li id="artDoi">
              <#include "../macro/doiAsLink.ftl" />
              <@doiAsLink article.doi />
            </li>
      </ul>

    </div>
  <div>

  </div>
</header>

