<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = '' />
<#assign cssFile="browse.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="static ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

    <section>
      <#include "volumesBody.ftl"/>
    </section>

<#include "../common/footer/footer.ftl" />
<@js src="resource/js/components/browse_volumes.js"/>
<@renderJs />

<@renderCssLinks />

</body>
</html>
