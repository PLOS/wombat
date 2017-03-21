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

<div class="search-results-controls">
  <div class="search-results-controls-first-row">
    <fieldset class="search-field">
      <legend>Search</legend>
      <label for="controlBarSearch">Search</label>
      <input id="controlBarSearch" type="text" pattern=".{1,}" name="q"
             value="${query}" required/>
      <button id="searchFieldButton" type="submit">
        <i class="search-icon"></i>
      </button>
      <i title="Clear Search Input" class="icon-times-circle clear"></i>
    </fieldset>
    <a id="advancedSearchLink" class="advanced-search-toggle-btn" href="#">Advanced Search</a>
    <a id="simpleSearchLink" class="advanced-search-toggle-btn" href="#">Simple Search</a>
    <a class="edit-query" href="#">Edit Query</a>
  </div>
  <div class="advanced-search-container">
  </div>

</div>
<#include "advancedSearchQueryBuilder.ftl" />