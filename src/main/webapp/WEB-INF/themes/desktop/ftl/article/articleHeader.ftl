<header class="title-block">
<#include "signposts.ftl" />
    <div class="article-meta">
    <#include "articleClassifications.ftl" />
    </div>
    <div class="article-title-etc">
    <#include "articleTitle.ftl" />

        <ul class="date-doi">
            <li id="artPubDate">Published: <@formatJsonDate date="${article.publicationDate}" format="MMMM d, yyyy" /></li>
            <li id="artDoi">
              <#include "../macro/doiAsLink.ftl" />
              <@doiAsLink article.doi />
            </li>

        <#macro crossPubTitle pub>
          <#if pub.italicizeTitle>
              <em>${pub.title}</em><#t/>
          <#else>
          ${pub.title}<#t/>
          </#if>
        </#macro>
        <#macro crossPubLink prefix publications>
        ${prefix}
          <#list publications as pub>
            <a href="${pub.href}"><@crossPubTitle pub /></a><#t/>
            <#if pub_has_next><#t/>,</#if>
          </#list>
        </#macro>
        <#if originalPub??>
            <li><@crossPubLink "Published in", [originalPub] /></li>
        </#if>
        <#if crossPub?size gt 0>
            <li><@crossPubLink "Featured in" crossPub /></li>
        </#if>

        </ul>

    <#include "revisionMenu.ftl" />

    </div>
</header>