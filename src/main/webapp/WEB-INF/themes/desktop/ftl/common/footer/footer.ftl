<div id="fig-viewer" class="reveal-modal" data-reveal>
<#include "figviewer.ftl" />
</div>
<div class="range-slider" data-slider>
  <span class="range-slider-handle" role="slider" tabindex="0"></span>
  <span class="range-slider-active-segment"></span>
  <input type="hidden">
</div>
</main><#-- opened in header.ftl -->

<footer id="pageftr">
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

