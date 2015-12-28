<div class="search-results-controls">
    <div class="search-results-controls-first-row">
        <fieldset class="search-field">
            <legend>Search</legend>
            <label for="controlBarSearch">Search</label>
            <input id="controlBarSearch" type="text" pattern=".{1,}" name="${advancedSearch?string('unformattedQuery', 'q')}"
                   value="${query}" required/>
        <#list activeFilterItems as item>
            <input type="hidden" name="${item.filterParamName}" value="${item.filterValue}"/>
        </#list>
        <#if (filterStartDate??)>
            <input type="hidden" name="filterStartDate" value="${filterStartDate}"/>
            <#if (filterEndDate??)>
                <input type="hidden" name="filterEndDate" value="${filterEndDate}"/>
            </#if>
        </#if>
            <button id="searchFieldButton" type="submit"><i class="search-icon"></i><i class="fa fa-times-circle fa-lg clear"></i></button>
        </fieldset>
        <a id="advancedSearchLink" class="search-results-advanced-search-submit" href="#">Advanced Search</a>
        <a id="simpleSearchLink" class="search-results-advanced-search-submit" href="#">Simple Search</a>
        <a class="edit-query" href="#">edit query</a>
    </div>
    <div class="advanced-search-container">
    </div>
</div>

<script type="text/template" id="advanced-search-row-template">
    <fieldset class="advanced-search-row">
        <div class="advanced-search-col-first">
            <select class="operator">
                <option value="AND">AND</option>
                <option value="OR">OR</option>
            </select>
        </div>
        <div class="advanced-search-col-3">
            <select class="category" title="Search Field">
                <% categories.forEach(function (category) { %>
                    <option value="<%= category.value %>"
                        <%= category.selected ?  'selected' : '' %>
                        <%= category.disabled ?  'disabled' : '' %>
                        data-input-template="<%= category.inputTemplate %>">
                            <%= category.name %>
                    </option>
                <% }); %>
            </select>
        </div>
        <div id="input-condition-container" class="advanced-search-col-7">
        </div>
        <div class="advanced-search-col-last">
            <a href="#">
                <i class="icon-minus remove-row-button"></i>
            </a>
            <a href="#">
                <i class="icon-plus-no-square add-row-button"></i>
            </a>
        </div>
    </fieldset>
</script>
<script type="text/template" id="advanced-search-controls">
    <form id="advanced-search-form" method="get" action="<@siteLink handlerName='advancedSearch'/>">
        <div class="advanced-search-inputs-container">
        </div>
        <div class="advanced-search-buttons-container">
            <input id="unformatted-query-input" type="hidden" name="unformattedQuery" value="${query}"/>
            <input type="submit" class="search-button" value="Search"/>
        </div>
    </form>
</script>
<script type="text/template" id="default-search-input">
    <div id="default-search-input-container">
        <input type="text" class="query-condition-value"/>
    </div>
</script>

<script type="text/template" id="date-search-input">
    <div id="date-search-input-container">
        <input id="date-search-query-input-from" type="date" class="query-condition-value"/>
        <input id="date-search-query-input-to" type="date" class="query-condition-value"/>
        <span id="date-search-query-input-to-span">to:&nbsp;&nbsp;</span>
    </div>
</script>
