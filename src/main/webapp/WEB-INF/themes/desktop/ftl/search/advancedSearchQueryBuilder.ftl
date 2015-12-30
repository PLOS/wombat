<script type="text/template" id="advanced-search-row-template">
    <#--
    This template represents an empty row with two selects
    at the beggining and plus/minus buttons at the end
    -->
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
    <#-- Main form and neccesary inputs to place all the parameters required in the querystring -->
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
    <#-- Default text input to write conditions -->
    <div id="default-search-input-container">
        <input type="text" class="query-condition-value"/>
    </div>
</script>

<script type="text/template" id="date-search-input">
    <#-- FROM and TO datepicker inputs to filter by date ranges -->
    <div id="date-search-input-container">
        <input id="date-search-query-input-from" type="text" placeholder="Date from (YYYY-MM-DD)" class="datepicker query-condition-value"/>
        <input id="date-search-query-input-to" type="text" placeholder="Date to (YYYY-MM-DD)" class="datepicker query-condition-value"/>
        <span id="date-search-query-input-to-span">to:&nbsp;&nbsp;</span>
    </div>
</script>
