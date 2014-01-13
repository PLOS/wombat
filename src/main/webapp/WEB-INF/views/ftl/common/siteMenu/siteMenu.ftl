<#include "siteMenuFlag.ftl" />
<#if hasSiteMenu>
<#macro menuSection title>
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
  <#include "siteMenuFooter.ftl" />
  </div>

</div>
</#if>
