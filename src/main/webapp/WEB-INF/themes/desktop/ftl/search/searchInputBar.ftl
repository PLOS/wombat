<div class="search-results-controls">
    <div class="search-results-controls-first-row">
        <fieldset class="search-field">
            <legend>Search</legend>
            <label for="controlBarSearch">Search</label>
            <input id="controlBarSearch" type="text" name="${advancedSearch?string('unformattedQuery', 'q')}"
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
            <button id="searchFieldButton" type="submit"><span class="search-icon"></span></button>
        </fieldset>
        <a id="advancedSearchLink" class="search-results-advanced-search-submit" href="${advancedSearchLink}">Advanced
            Search</a>
        <a class="edit-query" href="#">edit query</a>
    </div>
    <div class="advanced-search-container">
    </div>
</div>

<script type="text/template" id="advanced-search-row-template">
    <div class="advanced-search-row">
        <div class="advanced-search-col-first">
            <select class="operator">
                <option value="AND">AND</option>
                <option value="OR">OR</option>
            </select>
        </div>
        <div class="advanced-search-col-2">
            <select class="category">
                <option value="title">Title</option>
                <option value="author">Author</option>
                <option value="subject">Subject</option>
            </select>
        </div>
        <div class="advanced-search-col-8">
            <input type="text" class="query-condition-value"/>
        </div>
        <div class="advanced-search-col-last">
            <a href="#">
                <i class="fa fa-minus fa-2x remove-row-button"></i>
            </a>
            <a href="#">
                <i class="fa fa-plus fa-2x add-row-button"></i>
            </a>
        </div>
    </div>
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
