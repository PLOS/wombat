<#-- Custom FreeMarker directives  -->
<#assign formatJsonDate = "org.ambraproject.wombat.util.Iso8601DateDirective"?new()>

<header id="common-header">
    <div class="search-expanded">
        <div class="search-form-container coloration-bg">
            <input id="search-input" type="text" class="search-field" placeholder="Search articles...">

            <div class="search-buttons">
                <button id="search-cancel" class="rounded">cancel</button>
                <button id="search-execute" class="rounded">search</button>
            </div>
        </div>
    </div>
    <div id="site-header-container" class="coloration-border-top">

        <a id="site-menu-button">Site Menu</a>
        <a class="site-search-button color-active"><span class="icon">Search</span></a>
        <a><#include "siteLogo.ftl" /></a>

    </div>
    <nav id="article-menu" class="menu-bar">
        <ul>
            <li>
                <a id="menu-browse">Browse Topics</a>
            </li>
            <li>
                <a id="menu-saved">Saved Items</a>
            </li>
            <li>
                <a id="menu-recent">Recent History</a>
            </li>
        </ul>
    </nav>
</header>
