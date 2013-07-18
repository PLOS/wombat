<div id="container-main">

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
            <a><h1 id="site-logo">PLOS</h1></a>

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
    </header><#--end common-header-->

    <div id="article-content" class="content" data-article-id="0059893">
        <article class="article-item">
            <a class="save-article circular coloration-text-color" data-list-type="individual">x</a>
            <h5 class="item-title lead-in">${article.articleType}</h5>

            <h2 class="article-title">${article.title}</h2>

            <p class="author-list">
            <#list article.authors as author>
                <a class="author-info" data-author-id="${author_index}">
                ${author.fullName}</a><#if author_has_next><#-- no space -->,</#if>
            </#list>
            </p>

            <#if articleCorrections?? && articleCorrections?size &gt; 0>
              <#list articleCorrections as correction>
                <div class="retraction red-alert">
                  <h3>${correction.title}:</h3>
                  ${correction.body}
                </div>
                <div class="correction-alert coloration-text-color">
                  <span class="plos-font">e</span> Correction added

                  <#-- TODO: better date handling; apparently dates are coming as Strings from the JSON right now.  -->
                  <span class="bold">${correction.created?date("MMM d, yyyy hh:mm:ss a")?string("dd MMM yyyy")}</span>
                </div><#-- end correction -->
              </#list>
            </#if>
        <#-- In articleSuffix.ftl: Close <article class="article-item"> -->
        <#-- In articleSuffix.ftl: Close <div id="article-content"> -->
        <#-- In articleSuffix.ftl: Close <div id="container-main"> -->
