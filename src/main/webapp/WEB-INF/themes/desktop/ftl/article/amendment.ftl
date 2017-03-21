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

<#include "citation.ftl" />
<#macro amendmentNotice amendmentGroup title linkText index>
<div class="amendment amendment-${amendmentGroup.type} toc-section">
  <#assign tocTitle>
    <#t/>${title}<#if amendmentGroup.amendments?size gt 1> (${amendmentGroup.amendments?size})</#if>
  </#assign>
  <a data-toc="amendment-${index?c}" title="${tocTitle}" id="amendment-${index?c}" name="amendment-${index?c}"></a>

  <h2>${title}</h2>
  <#list amendmentGroup.amendments as amendment>
    <#if amendment.body??>
      <div class="amendment-body">
      ${amendment.body}
      </div>
    </#if>
    <div class="amendment-citation">
      <p>
        <#if amendment.publicationDate??>
          <span class="amendment-date">
            <@formatJsonDate date="${amendment.publicationDate}" format="d MMM yyyy" />:
          </span>
        </#if>

        <@displayCitation amendment />

        <#if amendment.doi??>
          <@siteLink path="article?id=" ; path>
            <span class="link-separator"> </span>
            <a href="${path + amendment.doi}" class="amendment-link">
            ${linkText}</a>
          </@siteLink>
        </#if>
      </p>
    </div>
  </#list>
</div>
</#macro>

<#list amendments as amendmentGroup>
  <#if amendmentGroup.type == 'correction'>
    <@amendmentNotice amendmentGroup,
    (amendmentGroup.amendments?size == 1)?string("Correction", "Corrections"),
    "View correction" amendmentGroup_index />
  </#if>
  <#if amendmentGroup.type == 'eoc'>
    <@amendmentNotice amendmentGroup "Expression of Concern" "View expression of concern" amendmentGroup_index />
  </#if>
  <#if amendmentGroup.type == 'retraction'>
    <@amendmentNotice amendmentGroup "Retraction" "View retraction" amendmentGroup_index />
  </#if>
</#list>
