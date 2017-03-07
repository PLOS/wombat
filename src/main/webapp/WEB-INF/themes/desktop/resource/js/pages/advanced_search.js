/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

/**
    Advanced search widget
 Provided with a inputSearchSelector this widget will create a list of rows that contain
 operators, condition and values that make a query builder interface for the user.
 The resulting query is pasted into the inputSearchSelector element.

 Be aware that the inputSearchSelector passed to the component should be an input[type=text] and
 its values and properties (such as disabled, readonly, etc) will be modified by the component.
 Also, in order to work with the current implementation, if the input has a parent fieldset then said
 element might also be disabled.
 
 */
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
    operatorSelectSelector  : 'select.operator',
    categorySelectSelector  : 'select.category',

    clearButtonSelector     : 'i.clear',

    /* Internal properties */
    maxConditions: 50,
    currentConditions: 0
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

    // Modify datepicker options
    RangeDatepicker.options.min = new Date(2003, 9, 1);
    RangeDatepicker.options.max = new Date();

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
        .on('change', this.categorySelectSelector, function (e, data) {
          var row = $(e.target).parents(that.rowSelector);
          var newInputTemplateSelector = $(e.target).find(':selected').data('input-template');

          /* default input template */
          if (!newInputTemplateSelector) {
            newInputTemplateSelector = '#default-search-input';
          }

          if (row.data('input-template-in-use') !== newInputTemplateSelector) {
            that.replaceRowInputTemplate(row, newInputTemplateSelector, data);
          }
          that.refreshSearchInput();
        })

        .on('template-replaced', function (e, row) {
          RangeDatepicker.init($(row).find(that.inputFromDateSelector), $(row).find(that.inputToDateSelector));
        })

        .data('advanced-search-initialized', true);

    $(this.inputSearchSelector).parents('form').on('submit', function (e) {
      e.preventDefault();
      that.validateForm(function (err) {
        if (err) return alert(err.message);
        if(_.isUndefined(window.SearchResult)) {
          $(e.target).unbind('submit').submit();
        }
      });
    });

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

    var searchInputPrevValue = $(this.inputSearchSelector).val();
    if (searchInputPrevValue.indexOf(':') !== -1) {
      searchInputPrevValue = '';
    }
    this.disableSearchInput();
    /* Add first row */
    this.addRow(searchInputPrevValue);

    if (cb && typeof cb === 'function') {
      cb();
    }
  };

  AdvancedSearch.isInitialized = function (containerSelector) {
    return $(containerSelector).data('advanced-search-initialized');
  };

  AdvancedSearch.addControlButtons = function () {
    var templateControls = _.template($(this.templateControlsSelector).html());
    $(this.containerSelector).prepend(templateControls());
  };

  AdvancedSearch.addRow = function (queryValue) {
    if (this.currentConditions >= this.maxConditions) {
      return alert('No more conditions can be added.');
    }
    // If no value, then it is initialized empty
    var templateRow = _.template($(this.templateRowSelector).html());
    $(this.inputContainerSelector).append(templateRow())
        /* trigger category change to process category input templates */
        .find(this.categorySelectSelector).trigger('change', queryValue);
    this.currentConditions++;
  };

  AdvancedSearch.removeRow = function (row) {
    row.remove();
    this.currentConditions--;
  };

  AdvancedSearch.replaceRowInputTemplate = function (row, newTemplateSelector, queryValue) {
    var inputTemplate = _.template($(newTemplateSelector).html());
    var rowInputContainer = row.find(this.inputConditionContainerSelector);
    rowInputContainer.children().remove();

    /* Append current input template */
    rowInputContainer.append(inputTemplate({queryValue: queryValue || ''}));
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
    $(this.inputSearchSelector).val(fullCondition).attr('advanced-condition', true);
    $(this.inputQuerySelector).val(fullCondition).attr('advanced-condition', true);
    if (fullCondition.length === 0) { // Mark inputs as empty
      $(this.inputSearchSelector).attr('advanced-condition', null);
      $(this.inputQuerySelector).attr('advanced-condition', null);
    }
  };

  AdvancedSearch.getRowQuery = function (row) {
    var query = '';
    query += row.find(this.operatorSelectSelector).val() + ' ';
    query += row.find(this.categorySelectSelector).val() + ':';

    var queryValue = '';
    if (row.find('input').hasClass('fdatepicker')) {
      /* Special treatement is required when inputs are datepickers */
      queryValue = this.processDateCondition(row.find('input'));
    } else {
      queryValue = row.find('input').val();
      if (queryValue.indexOf(' ') !== -1) {
        /* If there is a space in the value, add quotes */
        queryValue = '"' + queryValue + '"';
      }
    }

    query += escapeQuery(queryValue);
    return query;
  };

  //Lucene requires escaping of the following characters with a backslash:
  // + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
  function escapeQuery(query) {
    return query.replace(/([!*+&|()\[\]{}\^~?:"])/g, "\\$1");
  }

  AdvancedSearch.processDateCondition = function (dates) {
    var processedDates = '';
    dates.each(function (ix, dateInput) {
      if (!dateInput.value) {
        return;
      }

      processedDates += dateInput.value + (ix === 0 ? 'T00:00:00Z' : 'T23:59:59Z');
      if (ix !== (dates.length - 1)) {
        /* add ' TO ' to all elements but the lastone */
        processedDates += ' TO ';
      }
    });
    return '[' + processedDates + ']';
  };

  AdvancedSearch.disableSearchInput = function () {
    var that = this;
    // Has to disable the fieldset containing the input
    $(this.inputSearchSelector).attr('disabled', true)
        .parent('fieldset').addClass('disabled');
    $(this.inputSearchSelector).off('change');
  };

  AdvancedSearch.enableSearchInput = function (focusInput) {
    var that = this;
    $(this.inputSearchSelector).attr('disabled', false)
        .parent('fieldset').removeClass('disabled');

    // Reflect changes by the user in the hidden input
    $(this.inputSearchSelector).on('change', function () {
      $(that.inputQuerySelector).val($(this).val());
    });

    if (focusInput) {
      $(this.inputSearchSelector).focus();
    }
  };

  AdvancedSearch.validateForm = function (cb) {
    var error = null;
    var query = $(this.inputQuerySelector).val();
    if (query.length <= 0) {
      error = new Error('Search query cannot be empty.');
    }
    // Return false if error and also callback with the error
    cb(error);
    return !error;
  };

  AdvancedSearch.destroy = function (containerSelector) {
    if (AdvancedSearch.isInitialized(containerSelector)) {
      AdvancedSearch.enableSearchInput(true);
      $(containerSelector).off('click change keyup').data('advanced-search-initialized', false).children().remove();
      $(this.clearButtonSelector).off('click');
      if (!$('#search-alert-modal').hasClass('ajax')) {
        $(this.inputSearchSelector).val('').parents('form').off('submit');
      }
      this.currentConditions = 0;
      $(this.inputSearchSelector).attr('advanced-condition', null);
      $(this.inputQuerySelector).attr('advanced-condition', null);
    }
  };

})(jQuery);