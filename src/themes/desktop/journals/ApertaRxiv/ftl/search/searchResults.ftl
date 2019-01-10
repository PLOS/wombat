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

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" class="no-js">
<#assign title = "Search Results" />
<#assign cssFile="ApertaRxiv-search.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />



<@js target="resource/js/util/class.js"/>

<@js target="resource/js/components/pagination.js"/>
<@js target="resource/js/components/range_datepicker.js"/>
<@js target="resource/js/vendor/spin.js"/>
<@js target="resource/js/vendor/foundation-datepicker.min.js"/>
<@js target="resource/js/pages/advanced_search.js"/>
<@js target="resource/js/pages/search_results.js"/>
<@js target="resource/js/components/toggle.js"/>
<@js target="resource/js/vendor/underscore-min.js"/>

<@themeConfig map="journal" value="journalKey" ; v>
  <#assign journalKey = v />
</@themeConfig>
<@themeConfig map="journal" value="journalName" ; v>
  <#assign journalName = v />
</@themeConfig>

<#if !isNewSearch??>
  <#assign isNewSearch = false />
</#if>

<#if isNewSearch || (searchResults.numFound == 0)>
  <#assign advancedOpen = true />
</#if>

<#if RequestParameters.q??>
  <#assign query = RequestParameters.q?html />
<#elseif RequestParameters.unformattedQuery??>
  <#assign query = RequestParameters.unformattedQuery?html />
<#else>
  <#assign query = '' />
</#if>

<#include "suppressSearchFilter.ftl" />

<body class="static ${journalStyle} search-results-body search-results-ajax">
<#include "searchTemplates.ftl" />
<#include "../common/paginationTemplate.ftl" />

<#assign headerOmitMain = true />
<#include "../common/header/headerContainer.ftl" />
<form name="searchControlBarForm" id="searchControlBarForm" action="<@siteLink handlerName='simpleSearch'/>"
      method="get" <#if advancedOpen??> data-advanced-search="open"  </#if>
>
  <section class="search-results-header">
    <div class="results-number">

    </div>
  <#-- TODO: fix this select dropdown.  See comments in the .scss.  -->
    <div class="search-sort">

      <div class="search-results-select">
        <label for="sortOrder">
          <select name="sortOrder" id="sortOrder">
            <option value="RELEVANCE">Sort By: Relevance</option>
            <option value="DATE_NEWEST_FIRST">Sort By: Date, newest first</option>
            <option value="DATE_OLDEST_FIRST">Sort By: Date, oldest first</option>
          </select>
        </label>
      </div>

    </div>
</form>



<#--PG-shoudl this be a header?-->
<section class="results-container">

    <article id="search-results" class="searchResults">
      <h2 class="no-term">Please enter your search term above.</h2>
    </article>
    <div id="search-loading"></div>
</section>

<#include "../common/footer/footer.ftl" />

<@renderJs />
</body>

</html>
