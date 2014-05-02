<header>
    <#--top head slot-->
    <div id="topslot" class="head-top">
    <#include "topSlot.ftl" />
    </div>
    <div class="grid-container">
    <div id="pagehdr-wrap" class="highlight-border">
      <div id="pagehdr">
        <nav id="nav-user" class="nav">
          <ul class="nav-top">
          <#macro navTopItem href highlighted=false>
            <li <#if highlighted>class="highlighted"</#if>><a href="${href}"><#nested/></a></li>
          </#macro>
          <#include "navTop.ftl" />
          </ul>
        </nav>
        <div class="head-main">
        <div class="logo">
          <a href="${pathUp(depth!0 ".")}">${siteTitle}</a>
        </div>

        <#include "search.ftl" />

      <#include "../siteMenu/siteMenu.ftl" />
        </div>
      </div><#-- pagehdr-->
    </div>
</header>
<main>
  <div id="pagebdy-wrap"><#-- Closed in footer.ftl -->
    <div id="pagebdy"><#-- Closed in footer.ftl -->
