<#-- Specific css file will be added by head.ftl template -->
<#assign cssFile = "related-content.css" />

<#include "../common/headContent.ftl" />

<div id="content" class="related-content">
  <article>
    <#include "relatedContentBody.ftl" />
  </article>
</div>

</div><#-- end home-content -->

<#include "../common/footer/footer.ftl" />

</div><#-- end container-main -->

<#include "../common/siteMenu/siteMenu.ftl" />

<#include "../common/bodyJs.ftl" />

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/pages/related_content.js"/>
<@renderJs />

</body>
</html>