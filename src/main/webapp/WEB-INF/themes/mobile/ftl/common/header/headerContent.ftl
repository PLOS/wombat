
<header id="common-header">
  <#macro searchForm journal="">
  <div class="search-expanded">
    <div class="search-form-container coloration-bg">
      <form id="simpleSearchForm" action="<@siteLink handlerName='simpleSearch'/>" method="get">
        <input name="q" id="search-input" type="text" class="search-field" placeholder="Search articles...">
        <#if journal?has_content><input type="hidden" name="filterJournals" value="${journal}"/></#if>
        <div class="search-buttons">
          <button id="search-cancel" class="rounded" type="reset">cancel</button>
          <button id="search-execute" class="rounded" type="submit">search</button>
        </div>
      </form>
    </div>
  </div>
  </#macro>
  <#include "searchJournal.ftl"/>
  <div id="site-header-container" class="coloration-border-top">

  <#include "../siteMenu/siteMenuFlag.ftl" />
  <#if hasSiteMenu>
    <a id="site-menu-button">Site Menu</a>
  </#if>
    <a class="site-search-button color-active"><span class="icon">Search</span></a>
    <a href="<@siteLink path="." />"><#include "../siteLogo.ftl" /></a>

  </div>
  <nav id="article-menu" class="menu-bar">
    <ul>
      <li>
        <noscript>
          <a href="http://www.activatejavascript.org" target="_blank">For full functionality of this site, please enable JavaScript.</a>
        </noscript>
      </li>
    <@themeConfig map="taxonomyBrowser" value="hasTaxonomyBrowser" ; hasTaxonomyBrowser>
      <#if hasTaxonomyBrowser>
        <li>
          <a href="<@siteLink path="/subjectAreaBrowse" />" id="menu-browse">Browse Topics</a>
        </li>
      </#if>
    </@themeConfig>


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
