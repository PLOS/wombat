<html>

<#assign title = "Page Not Found" />
<#include "common/head.ftl" />

<body class="static ${journalKey?lower_case}">
<#include "common/header/header.ftl" />
<h1>Page Not Found - desktop</h1>
<div class="error-page">

  <div class="image">
    <img src="<@siteLink path="resource/img/404error.png"/>" alt="no image to display"/>
    <p class="caption">
      <a href="http://creativecommons.org/licenses/by/2.0/deed.en">CC</a>
      Image courtesy of
      <a href="http://www.flickr.com/photos/heypaul/107326169/">Hey Paul on Flickr</a>
    </p>
  </div>

  <div class="content">
    <p>Sorry, the page that you've requested cannot be found; it may have been moved, changed or removed.</p>

  <#include "common/legacyLink.ftl" />
    <p>
      Please use the
      <a href="${legacyUrlPrefix}search/advanced?noSearchFlag=true&query=">advanced search form</a>
      to locate an article.
    </p>

    <p>
      If you continue to experience problems with the site, please provide a detailed account of the circumstances on
      our <a href="${legacyUrlPrefix}feedback/new">feedback form</a>.
    </p>

    <p>Thank you for your patience.</p>
  </div>
</div>
<#include "common/footer/footer.ftl" />
<#include "common/bodyBottomJs.ftl" />

</body>
</html>

