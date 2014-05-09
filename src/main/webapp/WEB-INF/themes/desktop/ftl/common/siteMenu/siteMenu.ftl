<#include "siteMenuFlag.ftl" />
<#if hasSiteMenu>

  <#macro siteMenuCalloutHeadline>
  <h3><#nested/></h3>
  </#macro>
  <#macro siteMenuCalloutBulletList>
  <ul>
    <#nested/>
  </ul>
  </#macro>
  <#macro siteMenuCalloutButton href>
  <a class="btn" href="${href}">
    <#nested/>
  </a>
  </#macro>

  <#macro menuSection title containsCallout=false>
  <li id="mn-01">
    <a href="javascript:void(0);">${title}</a>

    <div class="submenu">
      <#if containsCallout>
        <div class="submit">
          <#include "siteMenuCallout.ftl" />
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
