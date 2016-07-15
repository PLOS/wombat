<ul class="author-list clearfix"  data-js-tooltip="tooltip_container" id="author-list">
<#include "maxAuthorsToShow.ftl" />

<#include "authorItem.ftl" />

<#if authors?size gt maxAuthorsToShow + 1>
<#--
  Put all authors in the range from maxAuthorsToShow-1 to size-1 in the expander.
  I.e., before clicking the expander, the user sees the first maxAuthorsToShow-1 authors and the last author.
  If the expander would contain only one author, just show the author instead.
  -->
  <#list authors as author><#-- Before the expander -->
    <#if author_index lt (maxAuthorsToShow - 1) >
      <@authorItem author author_index author_has_next true false true/>
    </#if>
  </#list>
      <#list authors as author><#-- Inside the expander -->

        <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
        <@authorItem author author_index author_has_next true true true/>
      </#if>

     </#list>


<li data-js-toggle="toggle_add">&nbsp;[ ... ],</li>

  <@authorItem authors[authors?size - 1] authors?size - 1 false false true/><#-- Last one after expander -->
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

