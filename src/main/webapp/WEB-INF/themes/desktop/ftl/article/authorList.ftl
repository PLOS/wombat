<ul class="author-list clearfix"  data-js-tooltip="tooltip_container" id="author-list">
<#include "maxAuthorsToShow.ftl" />
<#macro authorItem author author_index author_has_next if_expander=false hidden=false>
  <#assign hasMeta =
  author.equalContrib || author.deceased || author.corresponding??
  || (author.affiliations?? && author.affiliations?size gt 0) || author.currentAddress??
  || (author.customFootnotes?? && author.customFootnotes?size gt 0)  />

  <li  <#if hasMeta>data-js-tooltip="tooltip_trigger"</#if>
       <#if hidden> data-js-toggle="toggle_target" data-visibility= "none"</#if> >
  <#if hasMeta> <a  <#else> <span </#if>data-author-id="${author_index?c}" class="author-name<#if
!hasMeta> no-author-data</#if>" >

    ${author.fullName}<#-- no space
 --><#if author.equalContrib> <span class="contribute"> </span></#if><#-- no space
 --><#if author.customFootnotes?? && author.customFootnotes?size gt 0> <span class="rel-footnote"> </span></#if><#-- no space
 --><#if author.corresponding??> <span class="email">  </span></#if><#-- no space
 --><#if author.deceased>&#8224</#if><#-- no space
 --><#if author_has_next><#-- no space -->,</#if><#-- no space
    --><#if hasMeta> </a>  <#else> </span> </#if>


    <#if hasMeta>
      <div id="author-meta-${author_index?c}" class="author-info" data-js-tooltip="tooltip_target">
        <#if author.equalContrib>
          <p>
            <span class="contribute"> </span> Contributed equally to this work with:
            <#list equalContributors as contributor>
            ${contributor}<#if contributor_has_next>,</#if>
            </#list>
          </p>
        </#if>

        <#if author.deceased><p id="authDeceased-${author_index?c}">&dagger; Deceased.</p></#if>
        <#if author.corresponding??><p id="authCorresponding-${author_index?c}"> ${author.corresponding}</p></#if>
        <#if author.affiliations?? && author.affiliations?size gt 0>
          <p id="authAffiliations-${author_index?c}"><#if author.affiliations?size gt 1>Affiliations:<#else>Affiliation:</#if>
            <#list author.affiliations as affil>
            ${affil}<#if affil_has_next>, </#if>
            </#list>
          </p>
        </#if>

        <#if author.currentAddresses?? && author.currentAddresses?size gt 0>
          <p id="authCurrentAddress-${author_index?c}">
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
        <a data-js-tooltip="tooltip_close" class="close" id="tooltipClose${author_index?c}"> &#x02A2F; </a>
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
      <@authorItem author author_index author_has_next true false />
    </#if>
  </#list>
      <#list authors as author><#-- Inside the expander -->

        <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
        <@authorItem author author_index author_has_next true true />
      </#if>

     </#list>


<li data-js-toggle="toggle_add">&nbsp;[ ... ],</li>

  <@authorItem authors[authors?size - 1] authors?size - 1 false false /><#-- Last one after expander -->
  <li data-js-toggle="toggle_trigger" >
  <#--there was no way to not do this. -->
    <a class="more-authors active" id="authors-show">[ view all ]</a>
    </li>
  <li data-js-toggle="toggle_trigger" data-visibility="none">
    <a class="author-less" id="author-hide">[ view less ]</a>
  </li>
<#else>
<#-- List authors with no expander -->
  <#list authors as author>
    <@authorItem author author_index author_has_next />
  </#list>
</#if>

</ul><#-- end div.author-list -->
<@js src="resource/js/components/tooltip.js" />

