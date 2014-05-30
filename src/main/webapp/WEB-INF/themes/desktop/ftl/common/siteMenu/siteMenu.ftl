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
	<li class="has-dropdown">
		<a href="#">${title}</a>

    <#if containsCallout>
			<div class="submit" style="display:none">
        <#include "siteMenuCallout.ftl" />
			</div>
    </#if>

		<ul class="dropdown">
      <#nested/>
		</ul>

	</li>
  </#macro>

  <#macro menuLink href>
	<li>
		<a href="${href}"><#nested/></a>
	</li>
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