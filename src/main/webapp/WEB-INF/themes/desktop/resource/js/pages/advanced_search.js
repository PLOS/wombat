var AdvancedSearch = {};
(function ($) {
  /* AdvancedSearch attributes */
  AdvancedSearch = {
    /* External elements */
    inputSearchSelector     : '#controlBarSearch',

    /* Internal elements */
    templateRowSelector     : '#advanced-search-row-template',
    templateControlsSelector: '#advanced-search-controls',
    containerSelector       : '.advanced-search-container',
    inputContainerSelector  : '.advanced-search-inputs-container',
    rowSelector             : '.advanced-search-row',
    addSelector             : '.add-row-button',
    removeSelector          : '.remove-row-button',
    editQuerySelector       : '.edit-query',
    inputConditionContainerSelector  : '#input-condition-container',
    inputConditionSelector  : 'input.query-condition-value',
    inputQuerySelector      : '#unformatted-query-input',
    inputFromDateSelector   : '#date-search-query-input-from',
    inputToDateSelector     : '#date-search-query-input-to',

    clearButtonSelector     : '#searchFieldButton .clear'
  };

  /* AdvancedSearch methods */
  AdvancedSearch.init = function (containerSelector, cb) {
    var that = this;
    if (containerSelector) {
      this.containerSelector = containerSelector;
    }

    if ($(this.containerSelector).length === 0) {
      /* There is no container in which to initialize advanced search */
      return cb(new Error('Advanced Search: No valid container provided to initialize advanced search widget.'));
    } else if (this.isInitialized(this.containerSelector)) {
      /* Advanced search has already been initialized in this container */
      return cb(new Error('Advanced Search: Advanced search has already been initialized in this container.'));
    }

    $(this.containerSelector)
        /* Add row binding */
        .on('click', this.addSelector, function (e) {
          e.preventDefault();
          that.addRow();
        })

        /* Remove row binding */
        .on('click', this.removeSelector, function (e) {
          e.preventDefault();
          var row = $(e.target).parents(that.rowSelector);
          that.removeRow(row);
          that.refreshSearchInput();
        })

        /* Write in condition input */
        .on('keyup change', this.inputConditionSelector, function () {
          that.refreshSearchInput();
        })

        .on('change', 'select', function () {
          that.refreshSearchInput();
        })

        /* Change input type on category change */
        .on('change', 'select.category', function (e) {
          var row = $(e.target).parents(that.rowSelector);
          var newInputTemplateSelector = $(e.target).find(':selected').data('input-template');

          /* default input template */
          if (!newInputTemplateSelector) {
            newInputTemplateSelector = '#default-search-input';
          }

          if (row.data('input-template-in-use') !== newInputTemplateSelector) {
            that.replaceRowInputTemplate(row, newInputTemplateSelector);
          }
          that.refreshSearchInput();
        })

        .on('template-replaced', function (e, row) {
          $(row)
              .find('.datepicker').datepicker({
                changeMonth: true,
                changeYear: true,
                maxDate: '0',
                dateFormat: 'yy-mm-dd',
                yearRange: '2003:+0'
              }).end()

              //Start Date max date is the entered End Date. End Date min date is the entered Start Date.
              //Both Start and End Dates have a strict maximum of the current day
              .find(that.inputFromDateSelector).on('change', function(){
                $(row).find(that.inputToDateSelector).datepicker('option', 'minDate', this.value);
              }).end()

              .find(that.inputToDateSelector).on('change', function(){
                $(row).find(that.inputFromDateSelector).datepicker('option', 'maxDate', this.value);
              });
        })

        .data('advanced-search-initialized', true);

    /* Add search button */
    this.addControlButtons();

    $(this.editQuerySelector).on('click', function (e) {
      e.preventDefault();
      that.enableSearchInput(true);
    });

    $(this.clearButtonSelector).on('click', function (e) {
        e.preventDefault();
        that.resetInputs();
    });

    this.disableSearchInput();

    /* Add first row */
    this.addRow();

    if (cb && typeof cb === 'function') {
      cb();
    }
  };

  AdvancedSearch.isInitialized = function (containerSelector) {
    return $(containerSelector).data('advanced-search-initialized');
  };

  /* inputTemplate options specifies whether to use an html template or not and which one */
  var tmpOptions = [
    {name: '----- Popular -----',   value: '', disabled: true},
    {name: 'All Fields',   value: 'everything', selected: true},
    {name: 'Title',   value: 'title'},
    {name: 'Author',  value: 'author'},
    {name: 'Body', value: 'body'},
    {name: 'Abstract', value: 'abstract'},
    {name: 'Subject', value: 'subject'},
    {name: 'Publication Date',  value: 'publication_date', inputTemplate: '#date-search-input'},
    {name: '----- Other -----',   value: '', disabled: true},
    {name: 'Subject', value: 'subject'},
    {name: 'Accepted Date',  value: 'accepted_date', inputTemplate: '#date-search-input'},
    {name: 'Article DOI (Digital Object Identifier)', value: 'id'},
    {name: 'Article Type', value: 'article_type'},
    {name: 'Author Affiliations', value: 'author_affiliate'},
    {name: 'Competing Interest Statement', value: 'competing_interest'},
    {name: 'Conclusions', value: 'conclusions'},
    {name: 'Editor', value: 'editor'},
    {name: 'eNumber', value: 'elocation_id'},
    {name: 'Figure & Table Captions', value: 'figure_table_caption'},
    {name: 'Financial Disclosure Statement', value: 'financial_disclosure'},
    {name: 'Introduction', value: 'introduction'},
    {name: 'Issue Number', value: 'issue'},
    {name: 'Materials and Methods', value: 'materials_and_methods'},
    {name: 'Received Date', value: 'received_date', inputTemplate: '#date-search-input'},
    {name: 'References', value: 'reference'},
    {name: 'Results and Discussion', value: 'results_and_discussion'},
    {name: 'Supporting Information', value: 'supporting_information'},
    {name: 'Trial Registration', value: 'trial_registration'},
    {name: 'Volume Number', value: 'volume'}
  ];
  AdvancedSearch.getCategoryOptions = function () {
    /* @TODO: Remove hardcoded  options and replace with ajax call. Cache options for better performance */
    if (this.categoryOptions) {
      return this.categoryOptions;
    }
    /* @TODO: Replace with ajax call */
    this.categoryOptions = tmpOptions;
    return this.categoryOptions;
  };

  AdvancedSearch.addControlButtons = function () {
    var templateControls = _.template($(this.templateControlsSelector).html());
    $(this.containerSelector).prepend(templateControls());
  };

  AdvancedSearch.addRow = function () {
    var templateRow = _.template($(this.templateRowSelector).html());
    $(this.inputContainerSelector).append(templateRow({categories: this.getCategoryOptions()}))
        /* trigger category change to process category input templates */
        .find('select.category').trigger('change');
  };

  AdvancedSearch.removeRow = function (row) {
    row.remove();
  };

  AdvancedSearch.replaceRowInputTemplate = function (row, newTemplateSelector) {
    var inputTemplate = _.template($(newTemplateSelector).html());
    var rowInputContainer = row.find(this.inputConditionContainerSelector);
    rowInputContainer.children().remove();

    /* Append current input template */
    rowInputContainer.append(inputTemplate());
    /* Store template selector in div's data for future template-in-use checking */
    row.data('input-template-in-use', newTemplateSelector).trigger('template-replaced', row);
  };

  AdvancedSearch.resetInputs = function () {
    $(this.inputSearchSelector).val('');
    $(this.rowSelector).remove();
    this.addRow();
  };

  AdvancedSearch.refreshSearchInput = function () {
    var that = this,
        fullCondition = '';
    $(this.rowSelector).each(function () {
      var row = $(this);
      if (row.find('input').length && row.find('input')[0].value) {
        /* Extract the query if input is not empty */
        fullCondition = '(' + fullCondition + that.getRowQuery(row) + ') ';
      }
    });

    /* Replace starting/ending parentheses and AND|OR from the beggining of the string */
    fullCondition = fullCondition.trim().replace(/^(\()/,'').replace(/(\))$/,'').replace(/^(\(*?)(AND|OR)\s/,'$1');
    $(this.inputSearchSelector).val(fullCondition);
    $(this.inputQuerySelector).val(fullCondition);
  };

  AdvancedSearch.getRowQuery = function (row) {
    var query = '';
    query += row.find('select.operator').val() + ' ';
    query += row.find('select.category').val() + ':';

    var queryValue = '';
    if (row.find('input').hasClass('datepicker')) {
      /* Special treatement is required when inputs are datepickers */
      queryValue = this.processDateCondition(row.find('input'));
    } else {
      queryValue = row.find('input').val();
      if (queryValue.indexOf(' ') !== -1) {
        /* If there is a space in the value, add quotes */
        queryValue = '"' + queryValue + '"';
      }
    }

    query += queryValue;
    return query;
  };

  AdvancedSearch.processDateCondition = function (dates) {
    var processedDates = '';
    dates.each(function (ix, dateInput) {
      if (!dateInput.value) {
        /* @TODO: Check what needs to be done with an empty date */
        return;
      }

      processedDates += dateInput.value + 'T00:00:00Z';
      if (ix !== (dates.length - 1)) {
        /* add ' TO ' to all elements but the lastone */
        processedDates += ' TO ';
      }
    });
    return '[' + processedDates + ']';
  };

  AdvancedSearch.disableSearchInput = function () {
    $(this.inputSearchSelector).attr('disabled', true)
        .parent('fieldset').addClass('disabled');
  };

  AdvancedSearch.enableSearchInput = function (focusInput) {
    $(this.inputSearchSelector).attr('disabled', false)
        .parent('fieldset').removeClass('disabled');
    if (focusInput) {
      $(this.inputSearchSelector).focus();
    }
  };

  AdvancedSearch.destroy = function (containerSelector) {
    if (AdvancedSearch.isInitialized(containerSelector)) {
      AdvancedSearch.enableSearchInput();
      $(containerSelector).off('click change keyup').data('advanced-search-initialized', false).children().remove();
      $(this.inputSearchSelector).val('');
    }
  };

})(jQuery);