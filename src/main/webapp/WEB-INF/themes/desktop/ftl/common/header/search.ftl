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

<#--markup starts in SiteMenu.ftl: this li is part of the main nav ul -->

<#macro searchForm journal="">

<li id="navsearch" class="head-search">
  <form name="searchForm" action="<@siteLink handlerName='simpleSearch'/>" method="get">
        <fieldset>
            <legend>Search</legend>
            <label for="search">Search</label>
            <input id="search" type="text" name="q" placeholder="Search" required/>
            <button id="headerSearchButton" type="submit"><span class="search-icon"></span></button>

        </fieldset>
        <#if journal?has_content><input type="hidden" name="filterJournals" value="${journal}"/></#if>
    </form>

    <a id="advSearch"
       href="<@siteLink handlerName='newAdvancedSearch'/>">
      advanced search
    </a>

  </li>

</#macro>
<#include "searchJournal.ftl"/>


<@js src="resource/js/components/placeholder_style.js"/>
