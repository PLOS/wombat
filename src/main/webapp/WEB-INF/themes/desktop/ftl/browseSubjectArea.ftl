<html>
<#assign title = "Whatever the head title should be" />
<#include "common/head.ftl" /> <!-- Adjust number of '../' steps as needed -->
<body>
<#include "common/header/headerContainer.ftl" />

<h1>Example for Alex. It should be removed.</h1>
<h2>
  <p>Results Per page: ${selectedResultsPerPage}</p>
  <p>Page number: ${page}</p>
  <p>Selected Sort Order: ${selectedSortOrder}</p>
  <p> Subject also Known as Category: ${filterSubjects?first}</p>
</h2>
<#if searchResults.numFound != 0>
  <#list searchResults.docs as doc>
    <p>${doc.id}: ${doc.title} : ${doc.link} </p>
  </#list>
</#if>

<#include "common/footer/footer.ftl" />
</body>
</html>