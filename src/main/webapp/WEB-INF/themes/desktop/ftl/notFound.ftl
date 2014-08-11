<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = "Page Not Found" />
<#include "common/head.ftl" />

<body class="static ${journalKey?lower_case}">
<#include "common/header/header.ftl" />
<h1>Page Not Found</h1>
<div class="error-page">

  <div class="content">
    <p>Sorry, the page that you've requested cannot be found; it may have been moved, changed or removed.</p>

    <p>Thank you for your patience.</p>
  </div>
</div>
<#include "common/footer/footer.ftl" />
<@js src="resource/js/global.js" />
<@renderJs />

</body>
</html>

