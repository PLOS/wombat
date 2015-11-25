<html>
<#assign title = "Whatever the head title should be" />
<#include "common/head.ftl" /> <!-- Adjust number of '../' steps as needed -->
<body>
<#include "common/header/headerContainer.ftl" />

<h1>Example for Alex. It should be removed.</h1>
<#if searchResults.numFound != 0>
  <#list searchResults.docs as doc>
    <p>${doc.id}: ${doc.title} : ${doc.link}</p>
  </#list>
</#if>

<#include "common/footer/footer.ftl" />
</body>
</html>