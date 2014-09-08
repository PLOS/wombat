<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = "Page Not Found" />
<#include "common/head.ftl" />

<#include "common/journalStyle.ftl" />
<body class="static ${journalStyle}">
<#include "common/header/header.ftl" />

<article class="error-page">
  <h1>Page Not Found</h1>
  <div class="content">
    <p>Sorry, the page that you've requested cannot be found; it may have been moved, changed or removed.</p>

    <p>Thank you for your patience.</p>
  </div>
</article>
<#include "common/footer/footer.ftl" />
<@js src="resource/js/global.js" />
<@renderJs />

</body>
</html>

