<#include "downloads.ftl" />

<div class="aside-container">
  <#include "print.ftl" />
  <#include "share.ftl" />
</div>

<#include "crossmark.ftl" />

<#include "relatedArticles.ftl" />

<#if collectionIssues?keys?size gt 0>
  <#include "asideCollections.ftl" />
</#if>

<#include "subjectAreas.ftl" />

<#include "adSlotAside.ftl" />

<#include "twitterModule.ftl" />

<#if articleComments?size gt 0  >
<#include "comments.ftl"  />
</#if>


