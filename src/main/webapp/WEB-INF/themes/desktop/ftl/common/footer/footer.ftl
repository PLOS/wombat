</div><#-- Close pagebdy from header.ftl -->
</div><#-- Close pagebdy-wrap from header.ftl -->
</main>
<footer id="pageftr">
	<div class="row">

		<div class="large-6 columns">
    <#macro footerLogo src alt>
			<img src="${src}" alt="${alt}" class="logo-footer"/>
    </#macro>
    <#include "footerLogo.ftl" />
    <#include "footerCredits.ftl" />
    <#include "footerPrimaryLinks.ftl" />
		</div>

		<div class="large-3 columns">
    <#include "footerSecondaryLinks.ftl" />
		</div>

		<div class="large-3 columns">
    <#include "footerTertiaryLinks.ftl" />
		</div>

	</div>
</footer>

</div><#-- Close page-wrap from header.ftl -->
