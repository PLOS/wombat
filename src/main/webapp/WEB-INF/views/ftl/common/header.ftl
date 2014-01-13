<header id="common-header">
  <div class="search-expanded">
    <div class="search-form-container coloration-bg">
      <form id="simpleSearchForm" action="search" method="get">
        <input name="q" id="search-input" type="text" class="search-field" placeholder="Search articles...">

        <div class="search-buttons">
          <button id="search-cancel" class="rounded" type="reset">cancel</button>
          <button id="search-execute" class="rounded" type="submit">search</button>
        </div>
      </form>
    </div>
  </div>
  <div id="site-header-container" class="coloration-border-top">

  <#include "siteMenu/siteMenuFlag.ftl" />
  <#if hasSiteMenu>
    <a id="site-menu-button">Site Menu</a>
  </#if>
    <a class="site-search-button color-active"><span class="icon">Search</span></a>
    <a href=<@pathUp depth!0 "." />><#include "siteLogo.ftl" /></a>

  </div>
  <nav id="article-menu" class="menu-bar">
    <ul>
      <li>
        <a href="browse" id="menu-browse">Browse Topics</a>
      </li>

    <#-- TODO: implement.  Not MVP.
    <li>
      <a id="menu-saved">Saved Items</a>
    </li>
    -->

    <#-- TODO: implement.  Not MVP.
    <li>
      <a id="menu-recent">Recent History</a>
    </li>
    -->
    </ul>
  </nav>
</header>
