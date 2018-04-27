<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<#include "siteMenuFlag.ftl" />
<#if hasSiteMenu>

  <#macro siteMenuCalloutHeadline>
  <h3 class="callout-headline"><#nested/></h3>
  </#macro>

  <#macro siteMenuCalloutDescription>
  <div class="action-contain">
  <p class="callout-content">
    <#nested/>
  </p>
  </#macro>

  <#macro siteMenuCalloutButton href>
  <p class="button-contain">
    <a class="button button-default" href="<@siteLink path='s/' + href/>">
      <#nested/>
    </a>
  </p>
  </div> <!-- opens in siteMenuCalloutDescription -->
  </#macro>

  <#macro siteMenuCalloutSpecial buttonText buttonTarget linkText linkTarget>
  <p class="button-contain special">
    <a class="button button-default" href="${buttonTarget}">
     ${buttonText}
    </a>
    <a class="button-link" href="${linkTarget}">
      ${linkText}
    </a>
  </p>
  </div>  <!-- opens in siteMenuCalloutDescription -->

  </#macro>

  <#macro siteMenuCalloutSingleButton buttonText buttonTarget>
  <p class="button-contain special">
    <a class="button button-default" href="${buttonTarget}">
     ${buttonText}
    </a>
  </p>
  </div>  <!-- opens in siteMenuCalloutDescription -->

  </#macro>

  <#macro menuGroup title singleColumn=false containsCallout=false>

    <#if singleColumn>
      <#assign column="single">
      <#nested/>
    <#else>
      <#assign column="group">
    <li class="multi-col-parent menu-section-header has-dropdown" id="${title?lower_case?replace(" ","-")}">
    ${title}
      <div class="dropdown mega ">
        <ul class="multi-col" id="${title?lower_case?replace(" ","-")}-dropdown-list">
          <#nested/>
        </ul>
        <#if containsCallout>
          <div class="calloutcontainer">

            <#include "siteMenuCallout.ftl" />
          </div>
        </#if>
      </div>
    </li>
    </#if>
  <#--</li>-->

  </#macro>
  <#macro menuSection title >

  <li class="menu-section-header <#if column="single">has-dropdown </#if>" id="${title?lower_case?replace(" ","-")}">
    <span class="menu-section-header-title">  ${title} </span>

    <ul class="menu-section <#if column="single">dropdown </#if>"
        id="${title?lower_case?replace(" ","-")}-dropdown-list">
      <#nested/>
    </ul>

  </li>
  </#macro>

  <#macro menuLink href id=''>
  <li>
    <a href="${href}" <#if id?has_content>id=${id}</#if>><#nested/></a>
  </li>
  </#macro>

  <#macro menuSpecialSection title>
  <li data-js-tooltip-hover="trigger" class="subject-area menu-section-header">
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

<h1 class="logo">
  <a href="<@siteLink path="." />">${siteTitle}</a>
</h1>

<section class="top-bar-section"> <#--closed in headerContainer.ftl-->

<ul class="nav-elements">
  <#include "siteMenuItems.ftl" />
</#if>

<@js src="resource/js/vendor/jquery.hoverIntent.js"/>
<@js src="resource/js/components/menu_drop.js"/>
<@js src="resource/js/components/hover_delay.js"/>
