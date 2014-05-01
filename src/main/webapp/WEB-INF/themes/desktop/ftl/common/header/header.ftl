
<header>
<div id="page-wrap">
  <div id="topbanner" class="head-ad">

    <!-- Div for the ad at the top of journal home page-->
    <div class="center">
      <div class="title">Advertisement</div>
    THIS IS WHERE THE ADS GO
    </div>
  </div>

  <div id="pagehdr-wrap" class="highlight-border">
    <div id="pagehdr">
      <nav id="user" class="nav">
        <ul class="nav-top">
          <li><a href="http://www.plos.org">plos.org</a></li>
          <li><a href="https://plosone-rskonnord:43/ambra-registration/register.action">create account</a></li>
          <li class="btn-style"><a
              href="/user/secure/secureRedirect.action?goTo=%2Fhome.action" class="highlight-background">sign in</a>
          </li>
        </ul>
      </nav>
      <div class="logo">
        <a href="/"><img src="/images/logo.png" alt="PLOS Biology"></a>
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
