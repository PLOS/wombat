<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign cssFile="browse-issue.css"/>
<#include "../common/title/issuesTitle.ftl" />
<#assign title = issuesTitle />

<#include "../macro/removeTags.ftl" />
<#include "../macro/doiResolverLink.ftl" />
<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="browse-issue ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<section >
<#include "issuesBody.ftl" />
</section>

<#include "../common/footer/footer.ftl" />

<@js src="resource/js/components/scroll.js"/>
<@js src="resource/js/components/floating_nav.js"/>
<@js src="resource/js/pages/browse-issues.js"/>

<@renderJs />

</body>
</html>
