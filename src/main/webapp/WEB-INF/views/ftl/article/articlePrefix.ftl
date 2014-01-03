<div id="container-main">

<#include "../common/header.ftl" />

  <div id="article-content" class="content" data-article-id="0059893">
    <article class="article-item">

    <#-- TODO: implement save to article list.  Not MVP.
    <a class="save-article circular coloration-text-color" data-list-type="individual">x</a>
    -->

      <h5 class="item-title lead-in">${article.articleType!""}</h5>

      <h2 class="article-title">${article.title}</h2>

      <p class="author-list">
      <#include "maxAuthorsToShow.ftl" />
      <#macro authorItem author author_index author_has_next>
        <a class="author-info" data-author-id="${author_index?c}">
        ${author.fullName}</a><#if author_has_next><#-- no space -->,</#if>
      </#macro>

      <#if article.authors?size gt maxAuthorsToShow + 1>
      <#--
        Put all authors in the range from maxAuthorsToShow-1 to size-1 in the expander.
        I.e., before clicking the expander, the user sees the first maxAuthorsToShow-1 authors and the last author.
        If the expander would contain only one author, just show the author instead.
        -->
        <#list article.authors as author><#-- Before the expander -->
          <#if author_index lt (maxAuthorsToShow - 1) >
            <@authorItem author author_index author_has_next />
          </#if>
        </#list>
        <a class="author-more" class="more-authors active">[...view
        ${article.authors?size - maxAuthorsToShow} more...],</a>
          <span class="more-authors-list">
            <#list article.authors as author><#-- Inside the expander -->
            <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (article.authors?size - 1) >
              <@authorItem author author_index author_has_next />
            </#if>
            </#list>
          </span>
        <@authorItem article.authors[article.authors?size - 1] article.authors?size - 1 false /><#-- Last one after expander -->
        <a class="author-less" class="less-authors">[ view less ]</a>

      <#else>
      <#-- List authors with no expander -->
        <#list article.authors as author>
          <@authorItem author author_index author_has_next />
        </#list>
      </#if>

      </p><#-- end p.author-list -->

    <#if formalCorrections?? && formalCorrections?size &gt; 0>
      <div class="retraction red-alert">
        <span><h3>Formal Correction:</h3> This article has been <em>formally corrected</em> to address the following errors.</span>
        <ol>
          <#list formalCorrections as correction>
            <li>
            ${correction.truncatedBodyWithUrlLinkingNoPTags}
              (<a href="article/correction?uri=${correction.annotationUri}" class="expand">read formal correction</a>)
            </li>
          </#list>
        </ol>
      </div>
    </#if>
    <#-- In articleSuffix.ftl: Close <article class="article-item"> -->
    <#-- In articleSuffix.ftl: Close <div id="article-content"> -->
    <#-- In articleSuffix.ftl: Close <div id="container-main"> -->
