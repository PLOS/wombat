<header>
<#--top head slot-->
  <div id="topslot" class="head-top">
  <#include "topSlot.ftl" />
  </div>
  <div class="grid-container">
    <div id="pagehdr-wrap" class="highlight-border">
			<nav id="nav-user" class="nav">
				<ul class="nav-top">
        <#macro navTopItem href highlighted=false>
					<li <#if highlighted>class="highlighted"</#if>><a href="${href}"><#nested/></a></li>
        </#macro>
        <#include "navTop.ftl" />
				</ul>
			</nav>
      <div id="pagehdr">

        <div class="logo">
          <a href="<@siteLink path="." />">${siteTitle}</a>
        </div>

      <#include "../siteMenu/siteMenu.ftl" />
      <#include "search.ftl" />
      </div>
    </div><#-- pagehdr-->
  </div>
</header>
<main>
  <div id="pagebdy-wrap"><#-- Closed in footer.ftl -->
    <div id="pagebdy"><#-- Closed in footer.ftl -->
