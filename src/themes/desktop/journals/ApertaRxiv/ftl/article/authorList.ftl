<#include "maxAuthorsToShow.ftl" />

<dl class="article-authors hide author-list clearfix" id="authors"
    data-component="collapse" data-toggle="false"
    data-toggle-class="author-toggle" data-box-class="author-box">

<#if authors?size gt maxAuthorsToShow + 1>
  <#list authors as author>
    <#if author_index lt (maxAuthorsToShow - 1) >
      <@authorItemFull author author_index author_has_next true false />
    </#if>
  </#list>
  <#list authors as author>
    <#if author_index gte (maxAuthorsToShow - 1) && author_index lt (authors?size - 1) >
      <@authorItemFull author author_index author_has_next true true />
    </#if>
  </#list>

  <dt class="authors-show-more">
    <a id="authors-show-more">[ ... ]</a>
  </dt>

  <@authorItemFull authors[authors?size - 1] authors?size - 1 false false false />

  <dt class="authors-show-less hide">
    <a id="authors-show-less">[ show fewer ]</a>
  </dt>

<#else>
  <#list authors as author>
    <@authorItemFull author author_index author_has_next true false />
  </#list>
</#if>
</dl>

