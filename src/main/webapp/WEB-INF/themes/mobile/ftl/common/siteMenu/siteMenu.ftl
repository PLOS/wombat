<#include "siteMenuFlag.ftl" />
<#if hasSiteMenu>
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
      <h4 class="coloration-light-text"><#nested/></h4>
    </#macro>
    <#macro siteMenuCalloutBulletList>
      <ul class="std">
        <#nested/>
      </ul>
    </#macro>
    <#macro siteMenuCalloutButton href>
      <div><a class="rounded coloration-white-on-color" href="${href}">
        <#nested/>
      </a></div>
    </#macro>
    <div id="submit-manuscript-container">
      <#include "siteMenuCallout.ftl" />
    </div>
  </div>

</div>
</#if>
