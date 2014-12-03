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
  <li class="has-dropdown"  id="${title?lower_case?replace(" ","-")}">
  ${title}

    <#if containsCallout>
      <div class="calloutcontainer dropdown">
        <div class="submit" id="dropdown-callout-submit">
          <#include "siteMenuCallout.ftl" />
        </div>

        <ul class="dropdowncallout" id="${title?lower_case?replace(" ","-")}-dropdown-list">
          <#nested/>
        </ul>
      </div>
    <#else>

      <ul class="dropdown" id="${title?lower_case?replace(" ","-")}-dropdown-list">
        <#nested/>
      </ul>
    </#if>

  </li>
  </#macro>

  <#macro menuLink href>
  <li>
    <a href="${href}"><#nested/></a>
  </li>
  </#macro>

  <#macro menuSpecialSection title>
  <li data-js-tooltip-hover="trigger" class="subject-area">
    ${title}
     <#nested/>
  </li>
  </#macro>

  <#macro tooltip >
  <p data-js-tooltip-hover="target" class="subject-area-info">
    <#nested/>
  </p>
  </#macro>

<#--Markup starts here
MARKUP: using Foundation Top Bar for navigation -->
<ul class="logo">
  <li class="home-link">
    <h1><a href="<@siteLink path="." />">${siteTitle}</a></h1>
  </li>
</ul>
<section class="top-bar-section"> <#--closed in header.ftl-->

<ul class="nav-elements">
  <#include "siteMenuItems.ftl" />
</#if>

<@js src="resource/js/vendor/jquery.hoverIntent.js"/>
<@js src="resource/js/components/menu_drop.js"/>
<@js src="resource/js/components/hover_delay.js"/>
