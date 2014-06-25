<header>
<#--top head slot-->
	<div id="topslot" class="head-top">
  <#include "adSlotTop.ftl" />
	</div>
	<div class="grid-container">
		<div id="pagehdr-wrap" class="highlight-border">
			<div id="user" class="nav">
				<ul class="nav-user">
        <#macro navTopItem href highlighted=false>
					<li <#if highlighted>class="highlighted"</#if>><a href="${href}"><#nested/></a></li>
        </#macro>
        <#include "navTop.ftl" />
				</ul>
			</div>
			<div id="pagehdr">
				<div class="contain-to-grid">  <#--foundation class name. needed for top-bar use. -->
					<nav class="nav-main">

          <#include "../siteMenu/siteMenu.ftl" />

          <#include "search.ftl" />

						</ul>     <#--opened in siteMenu.ftl -->
						</section>  <#--opened in siteMenu.ftl -->
					</nav>
				</div>
			</div>
		</div><#-- pagehdr-->
	</div>
</header>
<main>
	<div id="pagebdy-wrap"><#-- Closed in footer.ftl -->
		<div id="pagebdy"><#-- Closed in footer.ftl -->
