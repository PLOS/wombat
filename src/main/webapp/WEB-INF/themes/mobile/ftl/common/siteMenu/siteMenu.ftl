<#include "siteMenuFlag.ftl" />
<#if hasSiteMenu>

  <#macro menuSpecialSection title>
  <!-- dont' do anything for mobile -->

  </#macro>
  <#macro siteMenuCalloutSpecial  buttonText buttonTarget linkText linkTarget >
  <p class="button-contain special">
    <a class="button button-default" href="${buttonTarget}">
    ${buttonText}
    </a>
    <a class="button-link" href="${linkTarget}">
    ${linkText}
    </a>
  </p>

  </#macro>
  <#macro menuGroup title singleColumn=false containsCallout=false>
    <#nested/>
  </#macro>

  <#macro menuSection title containsSubmit=false>
  <li class="accordion-item">
    <a class="expander">
      <span class="arrow">Expand</span>
    ${title}
    </a>

    <ul class="secondary accordion-content">
      <#nested>
    </ul>
  </li>
  </#macro>
  <#macro menuLink href>
  <li>
    <a class="btn-lg" href="${href}"><#nested></a>
  </li>
  </#macro>
  <#macro submitBlock>
  <#-- TODO Document -->
  </#macro>
<div id="common-menu-container" class="full-menu-container coloration-border-top">
  <nav class="full-menu">
    <ul class="primary accordion">
      <#include "siteMenuItems.ftl" />

      <#-- TODO: implement when we support logged-in functionality.
      <li class="accordion-item">
        <a class="expander">
          <span class="arrow">Expand</span>
          Create an Account
        </a>

        <p class="accordion-content secondary">[TEMP - CONTENT]</p>
      </li>
      <li class="accordion-item">
        <a class="expander">
          <span class="arrow">Expand</span>
          Sign In
        </a>

        <p class="accordion-content secondary">[TEMP - CONTENT]</p>
      </li>
      -->

    </ul>
  </nav>

  <div class="full-menu-tout">
    <#macro siteMenuCalloutHeadline>
      <h4 id="callout-headline" class="coloration-light-text"><#nested/></h4>
    </#macro>
    <#macro siteMenuCalloutDescription>

        <h5 id="callout-description"  class="pad-below"><#nested/></h5>

    </#macro>
    <#macro siteMenuCalloutButton href>
      <div><a class="rounded coloration-white-on-color" href="<@siteLink path='s/' + href/>" id="callout-button">
        <#nested/>
      </a></div>
    </#macro>
    <#macro siteMenuCalloutSpecial  buttonText buttonTarget linkText linkTarget >
      <div>
        <p class="pad-below">
          <a class="rounded coloration-white-on-color"  id="callout-button" href="${buttonTarget}">
          ${buttonText}
          </a>
        </p>
        <p>
          <a class="coloration-light-text header-style" id="callout-link" href="${linkTarget}">
          ${linkText}
          </a>
        </p>
      </div>

    </#macro>

    <div id="submit-manuscript-container">
      <#include "siteMenuCallout.ftl" />
    </div>
  </div>

</div>

</#if>
