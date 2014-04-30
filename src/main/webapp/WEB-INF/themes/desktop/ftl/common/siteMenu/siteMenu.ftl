<#include "siteMenuFlag.ftl" />
<#if hasSiteMenu>
  <#macro menuSection title containsSubmit=false>
  <li id="mn-01">
    <a href="javascript:void(0);">${title}</a>

    <div class="submenu">
      <#if containsSubmit>
      <div class="submit">
        <#include "siteMenuFooter.ftl" />
      </div>
      </#if>
      <div class="menu">
        <ul>
          <#nested/>
        </ul>
      </div>
    </div>
  </li>
  </#macro>
  <#macro menuLink href>
  <li>
    <a href="${href}"><#nested/></a>
  </li>
  </#macro>

<nav id="nav-main" class="nav">
  <ul>
    <#include "siteMenuItems.ftl" />
  </ul>
</nav>

</#if>
