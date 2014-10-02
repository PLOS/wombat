<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = article.title />
<#assign depth = 0 />
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
  <body class="article ${journalStyle}">

  <input type="hidden" id="rawPubDate" value="${article.date}" />

  <#include "../common/header/header.ftl" />
  <div class="set-grid">
    <header class="title-block">
      <div class="article-meta">
      <#include "articleClassifications.ftl" />
       <ul id="almSignposts" class="signposts">
         <li>1 billion<br>views</li>
       </ul>
      </div>
      <div class="article-title-etc">
      <#include "articleTitle.ftl" />

      <ul class="date-doi">
        <li id="artPubDate">Published:  </li>
        <li id="artDoi">DOI: ${article.doi} </li>

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
          <#if pub.href??>
            <a href="${pub.href}"><@crossPubTitle pub /></a><#t/>
          <#else>
            <@crossPubTitle pub /><#t/>
          </#if>
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
      </div>
    </header>
    <section class="article-body">

      <#include "tabs.ftl" />

      <div class="article-text" id="artText">
         ${articleText}
      </div>

    </section>
    <aside class="article-aside">
    <#include "aside/sidebar.ftl" />
    </aside>
  </div>
  <#include "../common/footer/footer.ftl" />

  <@js src="resource/js/components/dateparse.js"/>
  <@js src="resource/js/components/show_onscroll.js"/>
  <@js src="resource/js/components/tooltip_hover.js"/>
  <@js src="resource/js/components/truncate_elem.js"/>
  <@js src="resource/js/components/float_header.js"/>
  <@js src="resource/js/pages/article.js"/>
  <@renderJs />

  <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js" ></script>
  <script type="text/javascript" src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

  <#include "aside/crossmarkIframe.ftl" />

</body>
</html>
