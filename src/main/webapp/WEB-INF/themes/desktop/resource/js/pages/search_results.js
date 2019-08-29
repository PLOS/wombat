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

var SearchResult;

(function ($) {
  SearchResult = Class.extend({
    isInitialized: false,

    $resultListEl: $('#search-results'),
    $filtersEl: $('#searchFilters'),
    $filterHeaderEl: $('.header-filter'),
    $searchHeaderEl: $('.search-results-header'),
    $searchCounterEl: $('.results-number'),
    $searchBarForm: $('#searchControlBarForm'),
    $searchBarInput: $('#controlBarSearch'),
    $loadingEl: $('#search-loading'),
    $orderByEl: $('#sortOrder'),
    $searchFeedBtnEl: $('.search-feed'),
    $searchAlertOpenModalBtnEl: $('.open-search-alert-modal'),
    $searchAlertCloseModalBtnEl: $('.btn-cancel-savedsearch'),
    $searchAlertSaveBtnEl: $('#btn-save-savedsearch'),
    $searchAlertModalEl: $('#search-alert-modal'),
    resultPerPageElId: '#resultsPerPageDropdown',
    resetFiltersElId: '#clearAllFiltersButton',
    dateFilterFormClass: '.date-filter-form',
    dateFilterEndElId: '#dateFilterEndDate',
    dateFilterStartElId: '#dateFilterStartDate',
    searchLoadingSpin: new Spinner().spin(),

    currentSearchParams: {
      "filterJournals": null,
      "filterSubjects": null,
      "filterArticleTypes": null,
      "filterAuthors": null,
      "filterSections": null,
      "filterStartDate": null,
      "filterEndDate": null,
      "resultsPerPage": null,
      "unformattedQuery": null,
      "q": null,
      "sortOrder": null,
      "page": 1
    },
    currentUrlParams: null,
    searchEndpoint: 'search',
    ajaxSearchEndpoint: 'dynamicSearch',
    searchFeedEndpoint: 'search/feed/atom',

    searchFilters: {},
    searchActiveFilters: [],
    searchDateFilters: {
      "end": null,
      "start": null
    },
    results: [],
    resultTotalRecords: 0,
    resultsOffset: 0,
    pagination: null,
    ALMData: null,
    filtersParams: [
      "filterJournals",
      "filterSubjects",
      "filterArticleTypes",
      "filterAuthors",
      "filterSections",
      "filterStartDate",
      "filterEndDate"
    ],
    filtersTitles: {
      "journal": "Journal",
      "subject_area": "Subject Area",
      "article_type": "Article Type",
      "author": "Author",
      "section": "Where my keywords appear"
    },

    init: function () {
      var that = this;
      this.loadUrlParams();
      this.bindSearchEvents();
      var currentPage = this.getCurrentPage();
      var itemsPerPage = this.getResultsPerPage();
      this.pagination = new Pagination(currentPage, this.resultTotalRecords, itemsPerPage, function (currentPage) {
        that.currentSearchParams.page = currentPage;
        that.processRequest();
      });

      if (typeof SearchResultsALMData !== "undefined") {
        this.ALMData = new SearchResultsALMData();
      }

      this.$searchHeaderEl.hide();
      this.$filtersEl.hide();
      this.$loadingEl.append(this.searchLoadingSpin.el);

      if (this.currentSearchParams.unformattedQuery != null) {
        this.currentSearchParams.q = this.currentSearchParams.unformattedQuery;
      }
      if (this.currentSearchParams.q != null || this.checkFilters()) {
        this.processRequest();
      }
      else {
        $('.no-term').show();
        this.isInitialized = true;
      }
    },
    getCurrentPage: function () {
      if (this.currentSearchParams.page && this.currentSearchParams.page != null) {
        return parseInt(this.currentSearchParams.page);
      }
      else {
        return 1;
      }
    },
    getResultsPerPage: function () {
      if (this.currentSearchParams.resultsPerPage && this.currentSearchParams.resultsPerPage != null) {
        return parseInt(this.currentSearchParams.resultsPerPage);
      }
      else {
        return 15;
      }
    },
    scrollWindowToTop: function () {
      var offset = this.$searchBarForm.position().top;
      $('body').stop().animate({scrollTop: offset}, '500', 'swing');
    },
    showLoading: function () {
      this.$loadingEl.show();
      this.$resultListEl.hide();
    },
    hideLoading: function () {
      this.$loadingEl.hide();
      this.$resultListEl.show();
    },
    loadUrlParams: function () {
      var that = this;
      var urlVars = this.getJsonFromUrl();
      this.currentSearchParams = _.mapObject(this.currentSearchParams, function (item, key) {
        var urlVar = urlVars[key];

        if (key == 'filterSubjects' && _.has(urlVars, 'subject')) {
          var subjectParam = urlVars['subject'];

          if(!_.isEmpty(urlVar)) {
            if(_.isArray(urlVar)) {
              urlVar = urlVar.push(subjectParam);
            }
            else {
              urlVar = [urlVar, subjectParam];
            }
          }
          else {
            urlVar = [subjectParam];
          }
        }

        if (!_.isEmpty(urlVar)) {
          return urlVar;
        }
        else {
          return null;
        }
      });
    },
    checkFilters: function () {
      var that = this;
      var standaloneFilters = [
        'filterStartDate',
        'filterEndDate',
        'filterAuthors',
        'filterSubjects'
      ];
      var isStandalone = false;

      _.each(standaloneFilters, function (filter) {
        if (isStandalone == false && that.currentSearchParams[filter] != null) {
          isStandalone = true;
        }
      });

      return isStandalone;
    },
    createUrlParams: function () {
      var urlParams = '?';
      if (this.currentSearchParams.q == null && this.checkFilters()) {
        this.currentSearchParams.q = "";
      }
      _.each(this.currentSearchParams, function (item, key) {
        if (item != null) {
          if (_.isArray(item)) {
            _.each(item, function (item) {
              urlParams = urlParams + key + '=' + encodeURIComponent(item) + '&';
            });
          }
          else {
            urlParams = urlParams + key + '=' + encodeURIComponent(item) + '&';
          }
        }
      });
      this.currentUrlParams = urlParams.slice(0, -1).replace('%20', '+');
      this.updatePageUrl();
    },
    updatePageUrl: function () {
      if (this.isInitialized) {
        var title = document.title;
        var href = this.searchEndpoint + this.currentUrlParams;
        window.history.pushState(href, title, href);
        this.$searchFeedBtnEl.prop('href', this.searchFeedEndpoint + this.currentUrlParams);
      }
    },
    bindSearchEvents: function () {
      var that = this;
      this.$searchBarForm.on('submit', function (e) {
        e.preventDefault();
        e.stopPropagation();
        var inputValue = that.$searchBarInput.val();
        that.currentSearchParams.unformattedQuery = null;
        that.currentSearchParams.q = inputValue;
        that.currentSearchParams.page = 1;

        that.processRequest();
      });

      if(!_.isNull(that.currentSearchParams.sortOrder)) {
        this.$orderByEl.find('option[value="'+that.currentSearchParams.sortOrder+'"]').prop('selected', 'selected');
      }

      this.$orderByEl.on('change', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.currentSearchParams.sortOrder = $(this).val();
        that.currentSearchParams.page = 1;
        that.processRequest();
      });

      window.onpopstate = function () {
        that.isInitialized = false;
        that.loadUrlParams();
        that.processRequest();
      };

      $('body').on('click', '[data-filter-param-name]', function (e) {
        e.preventDefault();
        e.stopPropagation();
        var param = $(this).data('filter-param-name');
        var value = $(this).data('filter-value');

        if (param == 'filterDates') {
          that.currentSearchParams.filterStartDate = null;
          that.currentSearchParams.filterEndDate = null;
          that.searchDateFilters.start = that.currentSearchParams.filterStartDate;
          that.searchDateFilters.end = that.currentSearchParams.filterEndDate;
        }
        else {
          var currentValue = that.currentSearchParams[param];

          if (_.isArray(currentValue)) {
            var index = _.indexOf(currentValue, value);

            if (index == -1) {
              that.currentSearchParams[param].push(value);
            }
            else {
              that.currentSearchParams[param].splice(index, 1);
            }
          }
          else {
            if (currentValue == value) {
              that.currentSearchParams[param] = [];
            }
            else if (currentValue != null) {
              that.currentSearchParams[param] = [currentValue, value];
            }
            else {
              that.currentSearchParams[param] = [value];
            }
          }
        }
        that.currentSearchParams.page = 1;

        that.processRequest();
      }).on('change', this.resultPerPageElId, function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.currentSearchParams.resultsPerPage = $(this).val();
        that.currentSearchParams.page = 1;
        that.processRequest();
      }).on('click', this.resetFiltersElId, function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.currentSearchParams.filterStartDate = null;
        that.currentSearchParams.filterEndDate = null;
        that.searchDateFilters.start = that.currentSearchParams.filterStartDate;
        that.searchDateFilters.end = that.currentSearchParams.filterEndDate;

        _.each(that.filtersParams, function (filter) {
          that.currentSearchParams[filter] = null;
        });
        that.processRequest();
      }).on('click', '[data-show-more-items]', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.showMoreFilterItems($(this));
      }).on('click', '[data-show-less-items]', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.showLessFilterItems($(this));
      }).on('click', '.advanced-search-toggle-btn', function (e) {
        e.preventDefault();
        $('.advanced-search-toggle-btn, .edit-query').toggle();
        if (AdvancedSearch.isInitialized('.advanced-search-container')) {
          $('.advanced-search-container').slideUp(function () {
            // Only destroy after it has been hidden
            AdvancedSearch.destroy('.advanced-search-container');
          });
        } else {
          AdvancedSearch.init('.advanced-search-container', function (err) {
            if (err) return console.log(err.message);
            // Only show after it has been initialized
            $('.advanced-search-container').slideDown();
          });
        }
      }).on('submit', this.dateFilterFormClass, function (e) {
        e.preventDefault();
        e.stopPropagation();
        that.currentSearchParams.filterEndDate = $(that.dateFilterEndElId).val();
        that.currentSearchParams.filterStartDate = $(that.dateFilterStartElId).val();
        that.searchDateFilters['end'] = that.currentSearchParams.filterEndDate;
        that.searchDateFilters['start'] = that.currentSearchParams.filterStartDate;
        that.processRequest();
      });
      toggle_component.init();

      if ($('#searchControlBarForm').attr('data-advanced-search')) {
        $('#simpleSearchLink, .edit-query').show();
        $('#advancedSearchLink').hide();

        AdvancedSearch.init('.advanced-search-container', function (err) {
          // Only show after it has been initialized
          $('.advanced-search-container').show();
          $('.advanced-search-inputs-container input[type=text]').first().focus();
        });

      }

      this.$searchAlertOpenModalBtnEl.on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        that.$searchAlertModalEl.foundation('reveal', 'open');

        if (that.$searchAlertModalEl.hasClass('loggedIn')) {
          var textInput = $('#text_name_savedsearch');
          textInput.val('');
          $('#span_error_savedsearch').html('');
          textInput.focus();
          textInput[0].selectionStart = textInput[0].selectionEnd = textInput[0].value.length;
        }
      });

      this.$searchAlertCloseModalBtnEl.on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        that.$searchAlertModalEl.foundation('reveal', 'close');
      });

      this.$searchAlertSaveBtnEl.on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.processSearchAlert();
      });
    },

    showLessFilterItems: function ($btn) {
      $btn.parent('ul').find('li:gt(4)').fadeOut();
      $btn.hide();
      $btn.parent('ul').find('[data-show-more-items]').fadeIn();
    },
    showMoreFilterItems: function ($btn) {
      $btn.parent('ul').find('li:gt(4)').fadeIn();
      $btn.hide();
    },

    processSearchAlert: function () {
      var that = this;
      var name = $('#text_name_savedsearch').val();
      var query = $('#alert_query_savedsearch').val();
      var weekly = $('#cb_weekly_savedsearch').is(':checked');
      var monthly = $('#cb_monthly_savedsearch').is(':checked');

      $.ajax({
        type: 'POST',
        url: 'searchalert/add',
        data: 'name=' + encodeURIComponent(name)
        + "&query=" + encodeURIComponent(query)
        + (weekly ? "&frequency=weekly" : "") + (monthly ? "&frequency=monthly" : ""),
        dataType: 'json',
        success: function (response) {
          if (response.error) {
            var errorMessage = response.error;
            $('#span_error_savedsearch').html(errorMessage);
            return;
          }

          that.$searchAlertModalEl.foundation('reveal', 'close');
        },
        error: function (req, textStatus, errorThrown) {
          $('#span_error_savedsearch').html(errorThrown);
          console.log('error: ' + errorThrown);
        }
      });
    },

    mapActiveFilters: function (response) {

      this.searchActiveFilters = response.activeFilterItems;

      if (!_.isEmpty(response.filterStartDate)) {
        this.searchDateFilters['start'] = response.filterStartDate;
      }
      if (!_.isEmpty(response.filterEndDate)) {
        this.searchDateFilters['end'] = response.filterEndDate;
      }
    },

    updateAlertQueryInput: function (alertQuery) {
      var $input = $('#alert_query_savedsearch');
      $input.val(alertQuery);
    },

    processRequest: function () {
      var that = this;
      this.pagination.setCurrentPage(this.getCurrentPage());
      this.showLoading();
      this.createUrlParams();

      if (!this.isInitialized) {
        this.isInitialized = true;
      }

      if (_.isEmpty(this.currentSearchParams.q) && !this.checkFilters()) {
        this.showNoResults();
        return;
      }

      $.ajax({
        url: this.ajaxSearchEndpoint + this.currentUrlParams,
        method: 'GET',
        jsonp: 'callback',
        dataType: 'json',
        timeout: 20000,
        beforeSend: function(request) {
          request.setRequestHeader("Accept", "application/json;charset=UTF-8");
        },
        success: function (response) {
          that.$resultListEl.html('');
          if (response.cannotParseQueryError) {
            that.showParseError();
          }
          else if (response.unknownQueryError) {
            that.showRequestError();
          }
          else {
            that.resultTotalRecords = response.searchResults.numFound;

            that.mapActiveFilters(response);

            if (that.resultTotalRecords > 0) {
              that.results = response.searchResults.docs;
              var currentPage = that.getCurrentPage();
              var itemsPerPage = that.getResultsPerPage();
              that.pagination.setItemsPerPage(itemsPerPage);
              that.pagination.setTotalRecords(that.resultTotalRecords);
              that.pagination.setCurrentPage(currentPage);
              that.resultsOffset = response.searchResults.start;

              that.searchFilters = response.searchFilters;

              that.$searchHeaderEl.show();
              that.$filtersEl.show();

              if(response.alertQuery) {
                that.updateAlertQueryInput(response.alertQuery)
              }

              that.createFilters();
              that.updateCounterText();
              that.createResultList();

              that.hideLoading();

              that.ALMData.DOIlist = _.pluck(that.results, 'id');

              that.ALMData.setDOIList(that.ALMData.DOIlist);
              that.ALMData.processALMDataRequest();

              that.scrollWindowToTop();

            }
            else {
              that.showNoResults();
            }
          }
        },
        error: function (jqXHR, textStatus) {
          that.showRequestError();
        }
      });

    },
    showRequestError: function () {
      var errorTemplate = _.template($('#searchGeneralErrorTemplate').html());
      this.$resultListEl.append(errorTemplate());
      this.$searchHeaderEl.hide();
      this.$filtersEl.hide();
      this.hideLoading();
    },
    showParseError: function () {
      var errorTemplate = _.template($('#searchParseErrorTemplate').html());
      this.$resultListEl.append(errorTemplate());
      this.$searchHeaderEl.hide();
      this.$filtersEl.hide();
      this.hideLoading();
    },
    showNoResults: function () {
      var noResultsTemplate = _.template($('#searchNoResultsTemplate').html());
      
      this.hideLoading();
      this.$resultListEl.html(noResultsTemplate(this.currentSearchParams));
      this.createFilterHeader();
      this.$searchHeaderEl.hide();
      this.$filtersEl.hide();

    },
    createResultList: function () {
      var resultListTemplate = _.template($('#searchListItemTemplate').html());
      var list = resultListTemplate({results: this.results});

      this.$resultListEl.append(list);
      this.$resultListEl.append(this.pagination.createPaginationElement());
      this.createPageSelector();
    },
    createPageSelector: function () {
      if (this.resultTotalRecords > 0) {
        var pages = {
          15: false,
          30: false,
          60: false
        };
        var pagingTemplate = _.template($('#searchPagingSelectorTemplate').html());
        pages[this.getResultsPerPage()] = true;
        this.$resultListEl.append(pagingTemplate({pages: pages}));
      }
    },
    updateCounterText: function () {
      var template = _.template($('#searchResultsNumberTemplate').html());
      var templateData = {
        resultsCount: s.numberFormat(this.resultTotalRecords, 0),
        q: this.currentSearchParams.q
      };
      $('.results-number').html(template(templateData));

    },
    createFilters: function () {
      var that = this;

      this.$filtersEl.html('');
      this.$filterHeaderEl.html('');
      if (this.resultTotalRecords > 0) {
        var filterSectionTemplate = _.template($('#searchListFilterSectionTemplate').html());

        _.each(this.filtersTitles, function (filterTitle, key) {
          var filter = that.searchFilters[key];
          filter.filterTitle = filterTitle;
          if (filter.activeFilterItems.length > 0 || filter.inactiveFilterItems.length > 0) {
            that.$filtersEl.append(filterSectionTemplate(filter));
          }
        });

        $('.filterSection').each(function () {
          var items = $(this).find('ul:not(".active-filters") li:not(.toggle-trigger)');
          var $moreButton = $(this).find('[data-show-more-items]');
          var $lessButton = $(this).find('[data-show-less-items]');
          if (items.length > 5) {
            that.showLessFilterItems($moreButton);
          }
          else {
            $moreButton.hide();
            $lessButton.hide();
          }
        });

        // Create date filter section
        var filterDateTemplate = _.template($('#searchListFilterDateTemplate').html());
        this.$filtersEl.append(filterDateTemplate(this.searchDateFilters));
        var $dateFilterStartDateEl = $(this.dateFilterStartElId);
        var $dateFilterEndDateEl = $(this.dateFilterEndElId);
        RangeDatepicker.init($dateFilterStartDateEl, $dateFilterEndDateEl);


        this.createFilterHeader();
      }
    },
    createFilterHeader: function () {
      var activeFilters = this.searchActiveFilters;
      var filterHeaderTemplate = _.template($('#searchHeaderFilterTemplate').html());
      if (activeFilters.length || this.searchDateFilters.start != null && this.searchDateFilters.end != null) {
        this.$filterHeaderEl.html(filterHeaderTemplate({
          activeFilterItems: activeFilters,
          searchDateFilters: this.searchDateFilters
        }));
      }
    },
    getJsonFromUrl: function (hashBased) {
      var query;
      if (hashBased) {
        var pos = location.href.indexOf("?");
        if (pos == -1) return [];
        query = location.href.substr(pos + 1);
      } else {
        query = location.search.substr(1);
      }
      var result = {};
      query.split("&").forEach(function (part) {
        if (!part) return;
        part = part.split("+").join(" "); // replace every + with space, regexp-free version
        var eq = part.indexOf("=");
        var key = eq > -1 ? part.substr(0, eq) : part;
        var val = eq > -1 ? decodeURIComponent(part.substr(eq + 1)) : "";
        var from = key.indexOf("[");
        if (from == -1) {
          var key = decodeURIComponent(key);
          if (!_.isEmpty(result[key])) {
            if (_.isArray(result[key])) {
              result[key].push(val);
            }
            else {
              var val = [result[key], val];
              result[key] = val;
            }
          }
          else {
            result[key] = val;
          }
        }
        else {
          var to = key.indexOf("]");
          var index = decodeURIComponent(key.substring(from + 1, to));
          key = decodeURIComponent(key.substring(0, from));
          if (!result[key]) result[key] = [];
          if (!index) result[key].push(val);
          else result[key][index] = val;
        }
      });
      return result;
    }
  });

  new SearchResult();
})(jQuery);
