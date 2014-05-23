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

  <#macro menuSection title subject containsCallout=false>
  <li>
    <a href="#" data-dropdown="${subject}" data-options="is_hover:true">${title}</a>

    <#if containsCallout>
			<div class="menuCallout">
        <#include "siteMenuCallout.ftl" />
			</div>
    </#if>
		<ul id="${subject}" class="f-dropdown" data-dropdown-content>
      <#nested/>
		</ul>
  </li>
  </#macro>
  <#macro menuLink href>
  <li>
    <a href="${href}"><#nested/></a>
  </li>
  </#macro>

<nav id="nav-main" class="head-nav">
  <ul>
    <#include "siteMenuItems.ftl" />
  </ul>
</nav>

</#if>
