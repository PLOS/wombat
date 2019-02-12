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

<div id="article-content" class="content" data-article-id="0059893">
    <article class="article-item">

    <#-- TODO: implement save to article list.  Not MVP.
    <a class="save-article circular coloration-text-color" data-list-type="individual">x</a>
    -->

      <small class="item-title lead-in">${articleType.name}</small>
      <h2 class="article-title"><@xform xml=article.title/></h2>

      <p class="author-list">
      <#include "maxAuthorsToShow.ftl" />
      <#macro authorItem author author_index author_has_next>
        <a href="#" class="author-info" data-author-id="${author_index?c}">
        ${author.fullName}<#if author.onBehalfOf??>, ${author.onBehalfOf}</#if><#if author_has_next><#-- no space -->,</#if></a>
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
        <a class="author-more" class="more-authors active" data-js="show">[...view
        ${authors?size - maxAuthorsToShow} more...],</a>
          <span class="more-authors-list">
            <#list authors as author><#-- Inside the expander -->
            <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
              <@authorItem author author_index author_has_next />
            </#if>
            </#list>
          </span>
        <@authorItem authors[authors?size - 1] authors?size - 1 false /><#-- Last one after expander -->
        <a class="author-less" data-js="hide">[ view less ]</a>

      <#else>
      <#-- List authors with no expander -->
        <#list authors as author>
          <@authorItem author author_index author_has_next />
        </#list>
      </#if>

      </p><#-- end p.author-list -->
    <#if revisionMenu.revisions?size gt 1>
        <#include "revision/revisionMenu.ftl" />
      </#if>
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
          <h5 class="author-full-name">${author.fullName}<#if author.onBehalfOf??>, on behalf of ${author.onBehalfOf}</#if></h5>
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
            <p><#if author.affiliations?size gt 1><small class="lead-in">Affiliations:</small><#else><small class="lead-in">Affiliation:</small></#if>
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
          <#if author.orcid?? && author.orcid.authenticated>
            <p id="authOrcid-${author_index?c}">
              <a href="${author.orcid.value}">${author.orcid.value}</a>
            </p>
          </#if>
        </div>
      </#if>
    </#list>

    <#include "revision/revisionNotice.ftl" />

    <#macro amendment amendmentObjects amendmentType>
      <div class="retraction red-alert">
        <span><h5>
          <#nested/>
          <#if amendmentObjects?size == 1>
            <a href="article?id=${amendmentObjects[0].doi}">
              View ${amendmentType}
            </a>
          <#else>
            View
            <#list amendmentObjects as amendmentObject>
              <a href="article?id=${amendmentObject.doi}">
              ${amendmentType} ${amendmentObject_index + 1}</a><#if amendmentObject_has_next>,</#if>
            </#list>
          </#if>
        </h5></span>
      </div>
    </#macro>
    <#list amendments as amendmentGroup>
      <#if amendmentGroup.type == 'correction'>
        <@amendment amendmentGroup.amendments "correction">
          This article has been corrected.
        </@amendment>
      </#if>
      <#if amendmentGroup.type == 'eoc'>
        <@amendment amendmentGroup.amendments "expression of concern">
          There is an expression of concern about this article.
        </@amendment>
      </#if>
      <#if amendmentGroup.type == 'retraction'>
        <@amendment amendmentGroup.amendments "retraction">
          This article has been retracted.
        </@amendment>
      </#if>
    </#list>

    <#-- In articleSuffix.ftl: Close <article class="article-item"> -->
    <#-- In articleSuffix.ftl: Close <div id="article-content"> -->
    <#-- In articleSuffix.ftl: Close <div id="container-main"> -->
