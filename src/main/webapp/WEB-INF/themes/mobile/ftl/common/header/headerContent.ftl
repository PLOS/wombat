<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->


<header id="common-header">
  <#macro searchForm journal="">
  <div class="search-expanded">
    <div class="search-form-container coloration-bg">
      <form id="simpleSearchForm" action="<@siteLink handlerName='simpleSearch'/>" method="get">
      <div class="form-group">
        <label for="search-input" class="">Search articles</label>
        <input name="q" id="search-input" type="text" class="search-field">
      </div>
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
