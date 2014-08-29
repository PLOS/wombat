<p class="author-list">
<#include "maxAuthorsToShow.ftl" />
<#macro authorItem author author_index author_has_next>

  <a  class="author-info" data-author-id="${author_index?c}">
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

          <span class="more-authors-list"  data-js="toggle_target" data-initial="hide">
            <#list authors as author><#-- Inside the expander -->
            <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
              <@authorItem author author_index author_has_next />
            </#if>
            </#list>
          </span>
  [...]
  <@authorItem authors[authors?size - 1] authors?size - 1 false />,<#-- Last one after expander -->
  <a class="more-authors active" data-js="toggle_trigger" id="authors-show">[ view all ]</a>
  <a class="author-less" data-js="toggle_trigger" data-initial="hide" id="author-hide">[ view less ]</a>

<#else>
<#-- List authors with no expander -->
  <#list authors as author>
    <@authorItem author author_index author_has_next />
  </#list>
</#if>

</p><#-- end div.author-list -->