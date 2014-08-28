<ul class="author-list inline-list">
<#include "maxAuthorsToShow.ftl" />
<#macro authorItem author author_index author_has_next>

  <li><a href="#" data-author-id="${author_index?c}" data-js="tooltip_trigger" style="display: inline-block">
  ${author.fullName}</a><#if author_has_next><#-- no space -->,</#if>

  <#assign hasMeta = author.equalContrib?? || author.deceased?? || author.corresponding??
  || (author.affiliations?? && author.affiliations?size gt 0) || author.currentAddress??
  || (author.customFootnotes?? && author.customFootnotes?size gt 0) />
  <#if hasMeta>
  <div id="author-meta-${author_index?c}" data-js="tooltip_target" data-initial="hide">
    <a class="close" href="#">Close</a>
    <h2 class="author-full-name"><a href="www.google.com">${author.fullName}</a></h2>
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
  </li>
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
  <a class="more-authors active" data-js="toggle_trigger">[...view
  ${authors?size - maxAuthorsToShow} more...],</a>
          <span class="more-authors-list"  data-js="toggle_target" data-initial="hide">
            <#list authors as author><#-- Inside the expander -->
            <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
              <@authorItem author author_index author_has_next />
            </#if>
            </#list>
          </span>
  <@authorItem authors[authors?size - 1] authors?size - 1 false /><#-- Last one after expander -->
  <a class="author-less" data-js="toggle_trigger" data-initial="hide">[ view less ]</a>
<#else>
<#-- List authors with no expander -->
  <#list authors as author>
    <@authorItem author author_index author_has_next />

  </#list>
</#if>

</ul><#-- end div.author-list -->
