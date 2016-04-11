

</main><#-- opened in headerContainer.ftl -->

<footer id="pageftr">
  <div class="row">
    <div class="brand-column">
    <#macro footerLogo src alt>
      <img src="${src}" alt="${alt}" class="logo-footer"/>
    </#macro>
    <#include "footerLogo.ftl" />
      <p class="nav-special">
      <#include "footerCredits.ftl" />
      </p>
    <#include "footerPrimaryLinks.ftl" />
    </div>
    <div class="link-column">
    <#include "footerSecondaryLinks.ftl" />
    </div>
    <div class="link-column">
    <#include "footerTertiaryLinks.ftl" />
    </div>
  </div>

<@js src="resource/js/global.js" />

</footer>

