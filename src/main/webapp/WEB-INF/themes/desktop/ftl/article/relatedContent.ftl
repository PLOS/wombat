<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign cssFile="related-content.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<div class="set-grid">
<#include "articleHeader.ftl" />
  <section class="article-body">

  <#include "tabs.ftl" />

  <@displayTabList "Related" />
  <#include "relatedContentBody.ftl" />

  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>
</div>



<#include "../common/footer/footer.ftl" />
<#include "articleJs.ftl" />
<@js src="resource/js/vendor/foundation-datepicker.min.js"/>
<@js src="resource/js/pages/related_content.js"/>


<@renderJs />

<script type="text/javascript" src="http://www.google.com/recaptcha/api/js/recaptcha_ajax.js"></script>
<#include "aside/crossmarkIframe.ftl" />

</body>
</html>
