<#include "downloads.ftl" />

<div class="aside-container">
  <#include "print.ftl" />
  <#include "share.ftl" />
</div>

<#include "crossmark.ftl" />

<#if article.relatedArticles?size gt 0>
  <#include "relatedArticles.ftl" />
</#if>

<#include "subjectAreas.ftl" />

<#include "adSlotAside.ftl" />

<#include "twitterModule.ftl" />

<#if articleComments?size gt 0  >
<#include "comments.ftl"  />
</#if>


