<header>
  <div id="page-wrap">
    <div id="topslot" class="head-ad">
    <#include "topSlot.ftl" />
    </div>

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
        <div class="logo">
          <a href="${pathUp(depth!0 ".")}">${siteTitle}</a>
        </div>

        <#include "search.ftl" />

      <#include "../siteMenu/siteMenu.ftl" />

      </div><#-- pagehdr-->
    </div>
</header>
<section>
  <div id="pagebdy-wrap"><#-- Closed in footer.ftl -->
    <div id="pagebdy"><#-- Closed in footer.ftl -->
