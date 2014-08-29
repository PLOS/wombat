<ul class="author-list clearfix">
<#include "maxAuthorsToShow.ftl" />

<#macro authorItem author author_index author_has_next toggle = false >

  <li data-js="tooltip_trigger"  <#if toggle>data-js="toggle_target" dlayata-initial="hide"</#if> >
    <a data-author-id="${author_index?c}" class="author-name">
  ${author.fullName}<#if author_has_next><#-- no space -->,</#if>

    </a>


    <#assign hasMeta = author.equalContrib?? || author.deceased?? || author.corresponding??
    || (author.affiliations?? && author.affiliations?size gt 0) || author.currentAddress??
    || (author.customFootnotes?? && author.customFootnotes?size gt 0) />
    <#if hasMeta>
      <div id="author-meta-${author_index?c}" class="author-info" data-js="tooltip_target">

        <#if author.equalContrib>
          <p>
            Contributed equally to this work with:
            <#list equalContributors as contributor>
            ${contributor}<#if contributor_has_next>,</#if>
            </#list>
          </p>
        </#if>

        <#if author.deceased><p>† Deceased.</p></#if>
        <#if author.corresponding??><p><a href="google.com">${author.corresponding}</a></p></#if>
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
        <a data-js="tooltip_close">Close</a>
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
      <@authorItem author author_index author_has_next false/>
    </#if>
  </#list>

      <#list authors as author><#-- Inside the expander -->
        <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
          <@authorItem author author_index author_has_next  />
      </#if>
     </#list>


  <@authorItem authors[authors?size - 1] authors?size - 1 false /><#-- Last one after expander -->
  <li data-js="toggle_trigger"><a class="more-authors active">[view all]</a></li>
  <li data-js="toggle_trigger" data-initial="hide">
    <a class="author-less">[ view less ]</a>
  </li>
<#else>
<#-- List authors with no expander -->
  <#list authors as author>
    <@authorItem author author_index author_has_next />

  </#list>
</#if>

</ul><#-- end div.author-list -->
