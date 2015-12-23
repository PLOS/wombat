(function ($) {
  /* AdvancedSearch attributes */
  var AdvancedSearch = {
    /* External elements */
    searchInputSelector     : "#controlBarSearch",

    /* Internal elements */
    templateRowSelector     : "#advanced-search-row-template",
    templateControlsSelector: "#advanced-search-controls",
    containerSelector       : ".advanced-search-container",
    inputContainerSelector  : ".advanced-search-inputs-container",
    rowSelector             : ".advanced-search-row",
    addSelector             : ".add-row-button",
    removeSelector          : ".remove-row-button",
    editQuerySelector       : ".edit-query",
    conditionInputSelector  : "input.query-condition-value",
    formSelector            : ".advanced-search-form",
    inputQuerySelector      : "#unformatted-query-input"
  };

  /* AdvancedSearch methods */
  AdvancedSearch.init = function () {
    var that = this;
    $(this.containerSelector)
        /* Add row binding */
        .on("click", this.addSelector, function (e) {
          e.preventDefault();
          that.addRow();
        })

        /* Remove row binding */
        .on("click", this.removeSelector, function (e) {
          e.preventDefault();
          var row = $(e.target).parents(that.rowSelector);
          that.removeRow(row);
          that.refreshSearchInput();
        })

        /* Write in condition input */
        .on("keyup", this.conditionInputSelector, function () {
          that.refreshSearchInput();
        })

        .on("change", "select", function () {
          that.refreshSearchInput();
        });

    $(this.editQuerySelector).on("click", function (e) {
      e.preventDefault();
      that.enableSearchInput(true);
    });

/*
    $(this.formSelector).on("submit", function () {
    });
*/

    this.disableSearchInput();

    /* Add search button */
    this.addControlButtons();

    /* Add first row */
    this.addRow();
  };

  AdvancedSearch.addControlButtons = function () {
    var templateControls = _.template($(this.templateControlsSelector).html());
    $(this.containerSelector).prepend(templateControls());
  };

  AdvancedSearch.addRow = function () {
    var templateRow = _.template($(this.templateRowSelector).html());
    $(this.inputContainerSelector).append(templateRow());
  };

  AdvancedSearch.removeRow = function (row) {
    row.remove();
  };

  AdvancedSearch.refreshSearchInput = function () {
    var that = this,
        fullCondition = "";
    $(this.conditionInputSelector).each(function () {
      if (this.value) {
        /* Extract the query if input is not empty */
        var row = $(this).parents(that.rowSelector);
        fullCondition += that.getRowQuery(row) + " ";
      }
    });

    /* Replace AND|OR from the beggining of the string */
    var fullCondition = fullCondition.replace(/^(AND|OR)\s/,"");
    $(this.searchInputSelector).val(fullCondition);
    $(this.inputQuerySelector).val(fullCondition);
  };

  AdvancedSearch.getRowQuery = function (row) {
    var query = "";
    query += row.find("select.operator").val() + " ";
    query += row.find("select.category").val() + ":";
    query += row.find("input").val();
    return query;
  };

  AdvancedSearch.disableSearchInput = function () {
    $(this.searchInputSelector).attr("disabled", true)
        .parent("fieldset").addClass("disabled");
  };

  AdvancedSearch.enableSearchInput = function (focusInput) {
    $(this.searchInputSelector).attr("disabled", false)
        .parent("fieldset").removeClass("disabled");
    if (focusInput) {
      $(this.searchInputSelector).focus();
    }
  };

  AdvancedSearch.init();

})(jQuery);