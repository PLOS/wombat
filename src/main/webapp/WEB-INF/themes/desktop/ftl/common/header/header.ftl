<header>
  <div id="topslot" class="head-top">
    <#include "adSlotTop.ftl" />
  </div>

  <div id="user" class="nav">
    <ul class="nav-user">
    <#macro navTopItem href highlighted=false>
      <li <#if highlighted>class="highlighted"</#if>><a href="${href}"><#nested/></a></li>
    </#macro>
      <#include "navTop.ftl" />
    </ul>
  </div>
  <div id="pagehdr">

    <nav class="nav-main">

    <#include "../siteMenu/siteMenu.ftl" />

    <#include "search.ftl" />

        </ul>     <#--opened in siteMenu.ftl -->
      </section>  <#--opened in siteMenu.ftl -->
    </nav>
  </div><#-- pagehdr-->

</header>

<main> <#-- closed in footer.ftl -->


