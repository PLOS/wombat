</div><#-- Close pagebdy from header.ftl -->
</div><#-- Close pagebdy-wrap from header.ftl -->
</main>
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
</footer>

