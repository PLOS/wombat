<div id="fig-viewer" class="reveal-modal" data-reveal>
<#include "figviewer.ftl" />
</div>
</main><#-- opened in header.ftl -->

<footer id="pageftr">
  <div id="fig-search-results">
  <a class="figures" data-reveal-id="fig-viewer" data-doi="10.1371/journal.pmed.0010019">Figures</a>
  </div>
  <div class="row">
    <div class="brand-column">
    <#macro footerLogo src alt>
      <img src="${src}" alt="${alt}" class="logo-footer"/>
    </#macro>
    <#include "footerLogo.ftl" />
    <#include "footerCredits.ftl" />
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

