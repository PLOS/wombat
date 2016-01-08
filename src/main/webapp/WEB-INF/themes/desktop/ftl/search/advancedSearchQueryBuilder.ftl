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
                <option value="" disabled=>----- Popular -----</option>
                <option value="everything" selected>All Fields</option>
                <option value="title" >Title</option>
                <option value="author" >Author</option>
                <option value="body" >Body</option>
                <option value="abstract" >Abstract</option>
                <option value="subject" >Subject</option>
                <option value="publication_date" data-input-template="#date-search-input">Publication Date</option>
                <option value="" disabled>----- Other -----</option>
                <option value="accepted_date" data-input-template="#date-search-input">Accepted Date</option>
                <option value="id" >Article DOI (Digital Object Identifier)</option>
                <option value="article_type" >Article Type</option>
                <option value="author_affiliate" >Author Affiliations</option>
                <option value="competing_interest" >Competing Interest Statement</option>
                <option value="conclusions" >Conclusions</option>
                <option value="editor" >Editor</option>
                <option value="elocation_id" >eNumber</option>
                <option value="figure_table_caption" >Figure &amp; Table Captions</option>
                <option value="financial_disclosure" >Financial Disclosure Statement</option>
                <option value="introduction" >Introduction</option>
                <option value="issue" >Issue Number</option>
                <option value="materials_and_methods" >Materials and Methods</option>
                <option value="received_date" data-input-template="#date-search-input">Received Date</option>
                <option value="reference" >References</option>
                <option value="results_and_discussion" >Results and Discussion</option>
                <option value="supporting_information" >Supporting Information</option>
                <option value="trial_registration" >Trial Registration</option>
                <option value="volume" >Volume Number</option>
            </select>
        </div>
        <div id="input-condition-container" class="advanced-search-col-7">
        </div>
        <div class="advanced-search-col-last">
            <a href="#" class="remove-row-button">
                <i class="icon-minus"></i>
            </a>
            <a href="#" class="add-row-button">
                <i class="icon-plus-no-square"></i>
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
        <input type="text" class="query-condition-value" value="<%= queryValue %>"/>
    </div>
</script>

<script type="text/template" id="date-search-input">
    <#-- FROM and TO datepicker inputs to filter by date ranges -->
    <div id="date-search-input-container">
        <input id="date-search-query-input-from" type="text" placeholder="Date from (YYYY-MM-DD)" class="fdatepicker query-condition-value"/>
        <input id="date-search-query-input-to" type="text" placeholder="Date to (YYYY-MM-DD)" class="fdatepicker query-condition-value"/>
        <span id="date-search-query-input-to-span">to:&nbsp;&nbsp;</span>
    </div>
</script>
