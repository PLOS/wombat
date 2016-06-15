<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = "Page Not Found" />
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">
<#include "../common/header/headerContainer.ftl" />

<article class="error-page">
  <h1>Page Not Found</h1>
  <div class="content">
    <p>Looking for an article? Use the article search box above, or try <a href="<@siteLink handlerName="advancedSearch" />">advanced search form</a>.</p>

    <p>Experiencing an issue with the website? Please use the <a href="<@siteLink handlerName="feedback" />">feedback form</a> and provide a detailed description of the
      problem.</p>
  </div>
</article>
<#include "../common/footer/footer.ftl" />
<@js src="resource/js/global.js" />
<@renderJs />

</body>
</html>

