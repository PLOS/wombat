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
        <a href="#" class="author-info" data-author-id="${author_index?c}">
        ${author.fullName}</a><#if author_has_next><#-- no space -->,</#if>
      </#macro>

      <#if authors?size gt maxAuthorsToShow + 1>
      <#--
        Put all authors in the range from maxAuthorsToShow-1 to size-1 in the expander.
        I.e., before clicking the expander, the user sees the first maxAuthorsToShow-1 authors and the last author.
        If the expander would contain only one author, just show the author instead.
        -->
        <#list authors as author><#-- Before the expander -->
          <#if author_index lt (maxAuthorsToShow - 1) >
            <@authorItem author author_index author_has_next />
          </#if>
        </#list>
        <a class="author-more" class="more-authors active">[...view
        ${authors?size - maxAuthorsToShow} more...],</a>
          <span class="more-authors-list">
            <#list authors as author><#-- Inside the expander -->
            <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
              <@authorItem author author_index author_has_next />
            </#if>
            </#list>
          </span>
        <@authorItem authors[authors?size - 1] authors?size - 1 false /><#-- Last one after expander -->
        <a class="author-less" class="less-authors">[ view less ]</a>

      <#else>
      <#-- List authors with no expander -->
        <#list authors as author>
          <@authorItem author author_index author_has_next />
        </#list>
      </#if>

    </p><#-- end p.author-list -->

    <#-- Render the hidden divs that display author affiliation (and other) info.
         These are displayed when clicking on an author's name.  We do this after
         rendering the list of author links, since it messes up the formatting
         otherwise.                                                          -->
    <#list authors as author>
      <#assign hasMeta = author.equalContrib?? || author.deceased?? || author.corresponding??
      || (author.affiliations?? && author.affiliations?size gt 0) || author.currentAddress??
      || (author.customFootnotes?? && author.customFootnotes?size gt 0) />
      <#if hasMeta>
        <div id="author-meta-${author_index?c}" style="display:none;">
          <h2 id="author-full-name">${author.fullName}</h2>
          <#if author.equalContrib>
              <p>
                Contributed equally to this work with:
                <#list equalContributors as contributor>
                  ${contributor}<#if contributor_has_next>,</#if>
                </#list>
              </p>
          </#if>
          <#if author.deceased><p>† Deceased.</p></#if>
          <#if author.corresponding??><p>${author.corresponding}</p></#if>
          <#if author.affiliations?? && author.affiliations?size gt 0>
            <p><#if author.affiliations?size gt 1>Affiliations:<#else>Affiliation:</#if>
              <#list author.affiliations as affil>
                ${affil}<#if affil_has_next>, </#if>
              </#list>
            </p>
          </#if>
          <#if author.currentAddresses?? && author.currentAddresses?size gt 0>
            <p>
              <#list author.currentAddresses as address>
                ${address}<#if address_has_next>; </#if>
              </#list>
            </p>
          </#if>
          <#if author.customFootnotes?? && author.customFootnotes?size gt 0>
            <#list author.customFootnotes as note>
              ${note}
            </#list>
          </#if>
        </div>
      </#if>
    </#list>

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
