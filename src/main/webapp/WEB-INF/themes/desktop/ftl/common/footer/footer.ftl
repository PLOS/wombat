</div><#-- Close pagebdy from header.ftl -->
</div><#-- Close pagebdy-wrap from header.ftl -->
</main>
<footer id="pageftr">
  <div class="ftr-cols cf">

    <div class="col col-1">
    <#macro footerLogo src alt>
      <img src="${src}" alt="${alt}" class="logo"/>
    </#macro>
    <#include "footerLogo.ftl" />
    <#include "footerCredits.ftl" />
    <#include "footerPrimaryLinks.ftl" />
    </div>

    <div class="col col-2">
    <#include "footerSecondaryLinks.ftl" />
    </div>

    <div class="col col-3">
    <#include "footerTertiaryLinks.ftl" />
    </div>

  </div>
</footer>

</div><#-- Close page-wrap from header.ftl -->
