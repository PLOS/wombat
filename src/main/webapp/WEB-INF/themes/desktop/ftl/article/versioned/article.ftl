<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = "TITLE", articleDoi = doi />
<#assign depth = 0 />

<#include "../../common/head.ftl" />
<#include "../../common/journalStyle.ftl" />

<body class="article ${journalStyle}">

<#include "../../common/header/header.ftl" />

<div class="versionList">
  <ul>
  <#list revisionList as r>
    <li><a href="versioned?id=${doi}&r=${r}">Revision ${r}</a></li>
  </#list>
  </ul>
</div>

<div class="articleBody">
  <pre>
    ${manuscript?replace('<', '&lt;')}
  </pre>
</div>

<#include "../../common/footer/footer.ftl" />

</body>
</html>
