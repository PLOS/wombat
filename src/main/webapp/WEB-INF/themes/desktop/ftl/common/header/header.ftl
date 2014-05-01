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

        <div id="db">
          <form name="searchForm" action="/search/simple?noSearchFlag=true&amp;query=" method="get">
            <input type="hidden" name="from" value="globalSimpleSearch" id="from"/><input type="hidden"
                                                                                          name="filterJournals"
                                                                                          id="filterJournals"/>
            <fieldset>
              <legend>Search</legend>
              <label for="search">Search</label>

              <div class="wrap">
                <input id="search" type="text" name="query" placeholder="Search">
                <input type="image" alt="SEARCH" src="/images/icon.search.gif">
              </div>
            </fieldset>
          </form>
          <a id="advSearch" href="/search/advanced?noSearchFlag=true&amp;query=&filterJournals=PLoSBiology">advanced
            search</a>
        </div>

      <#include "../siteMenu/siteMenu.ftl" />

      </div><#-- pagehdr-->
    </div>
</header>
<section>
  <div id="pagebdy-wrap"><#-- Closed in footer.ftl -->
    <div id="pagebdy"><#-- Closed in footer.ftl -->
