var SearchResult;

(function ($) {
  SearchResult = Class.extend({
    isInitialized: false,

    $resultListEl: $('.searchResults'),
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

      this.ALMData = new SearchResultsALMData();

      this.$searchHeaderEl.hide();
      this.$filtersEl.hide();

      if(this.currentSearchParams.q != null) {
        this.processRequest();
      }
      else {
        this.isInitialized = true;
      }
    },
    getCurrentPage: function() {
      if(this.currentSearchParams.page && this.currentSearchParams.page != null) {
        return parseInt(this.currentSearchParams.page);
      }
      else {
        return 1;
      }
    },
    getResultsPerPage: function () {
      if(this.currentSearchParams.resultsPerPage && this.currentSearchParams.resultsPerPage != null) {
        return parseInt(this.currentSearchParams.resultsPerPage);
      }
      else {
        return 15;
      }
    },
    showLoading: function () {
      this.$loadingEl.show();
    },
    hideLoading: function () {
      this.$loadingEl.hide();
    },
    loadUrlParams: function () {
      var that = this;
      var urlVars = this.getJsonFromUrl();
      this.currentSearchParams = _.mapObject(this.currentSearchParams, function (item, key) {
        var urlVar = urlVars[key];
        if(!_.isEmpty(urlVar)) {
          return urlVar;
        }
        else {
          return null;
        }
      });
    },
    createUrlParams: function () {
      var urlParams = '?';
      _.each(this.currentSearchParams, function (item, key) {
        if(item != null) {
          if(_.isArray(item)) {
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
      if(this.isInitialized) {
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

      this.$orderByEl.on('change', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.currentSearchParams.sortOrder = $(this).val();
        that.currentSearchParams.page = 1;
        that.processRequest();
      });

      $('body').on('click', '[data-filter-param-name]', function (e) {
        e.preventDefault();
        e.stopPropagation();
        var param = $(this).data('filter-param-name');
        var value = $(this).data('filter-value');

        var currentValue = that.currentSearchParams[param];

        if(_.isArray(currentValue)) {
          var index = _.indexOf(currentValue, value);

          if (index == -1) {
            that.currentSearchParams[param].push(value);
          }
          else {
            that.currentSearchParams[param].splice(index, 1);
          }
        }
        else {
          if(currentValue == value) {
            that.currentSearchParams[param] = [];
          }
          else if(currentValue != null) {
            that.currentSearchParams[param] = [currentValue, value];
          }
          else {
            that.currentSearchParams[param] = [value];
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
      });

      this.$searchAlertOpenModalBtnEl.on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.$searchAlertModalEl.foundation('reveal', 'open');

        if(that.$searchAlertModalEl.hasClass('loggedIn')) {
          var textInput = $('#text_name_savedsearch');
          textInput.val(that.currentSearchParams.q);
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
      var query = {};

      _.mapObject(this.currentSearchParams, function (item, key) {
        if(!_.isEmpty(item)) {
          query[key] = item;
        }
      });
      query = JSON.stringify(query);
      var weekly = $('#cb_weekly_savedsearch').is(':checked');
      var monthly = $('#cb_monthly_savedsearch').is(':checked');

      $.ajax({
        type: 'POST',
        url: 'searchalert/add',
        data: 'name=' + encodeURIComponent(name)
        + "&query=" + encodeURIComponent(query)
        + (weekly ? "&frequency=weekly" : "") + (monthly ? "&frequency=monthly" : ""),
        dataType:'json',
        success: function(response) {
          if (response.error) {
            var errorMessage = response.error;
            $('#span_error_savedsearch').html(errorMessage);
            return;
          }

          that.$searchAlertModalEl.foundation('reveal', 'close');
        },
        error: function(req, textStatus, errorThrown) {
          $('#span_error_savedsearch').html(errorThrown);
          console.log('error: ' + errorThrown);
        }
      });
    },

    processRequest: function () {
      var that = this;
      this.pagination.setCurrentPage(this.getCurrentPage());
      this.showLoading();
      this.createUrlParams();

      if(!this.isInitialized) {
        this.isInitialized = true;
      }

      $.ajax({
        url: this.ajaxSearchEndpoint+this.currentUrlParams,
        method: 'GET',
        jsonp: 'callback',
        dataType: 'json',
        timeout: 20000,
        success: function (response) {
          that.$resultListEl.html('');
          if(response.cannotParseQueryError) {
            that.showParseError();
          }
          else if (response.unknownQueryError) {
            that.showRequestError();
          }
          else {
            that.resultTotalRecords = response.searchResults.numFound;
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

              that.createFilters();
              that.updateCounterText();
              that.createResultList();

              that.hideLoading();

              that.ALMData.setDOIList();
              that.ALMData.processALMDataRequest();

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
      this.createFilters();
    },
    showParseError: function () {
      var errorTemplate = _.template($('#searchParseErrorTemplate').html());
      this.$resultListEl.append(errorTemplate());
      this.$searchHeaderEl.hide();
      this.$filtersEl.hide();
      this.createFilters();
    },
    showNoResults: function() {
      var noResultsTemplate = _.template($('#searchNoResultsTemplate').html());

      this.$resultListEl.append(noResultsTemplate(this.currentSearchParams));
      this.$searchHeaderEl.hide();
      this.$filtersEl.hide();
      this.createFilters();

    },
    createResultList: function () {
      var resultListTemplate = _.template($('#searchListItemTemplate').html());

      var list = resultListTemplate({results: this.results});

      this.$resultListEl.append(list);
      this.$resultListEl.append(this.pagination.createPaginationElement());

      this.createPageSelector();
    },
    createPageSelector: function () {
      if(this.resultTotalRecords > 0) {
        var pages = {
          15: false,
          30: false,
          60: false
        };
        var pagingTemplate = _.template($('#searchPagingSelectorTemplate').html());
        pages[this.getResultsPerPage()] = true;
        this.$resultListEl.append(pagingTemplate({ pages:pages }));
      }
    },
    updateCounterText: function () {
      var counterText = '%TOTAL% %RESULTSTR% for <strong>%TERM%</strong>';
      counterText = counterText.replace('%TOTAL%', this.resultTotalRecords);
      if(this.resultTotalRecords > 1) {
        counterText = counterText.replace('%RESULTSTR%', 'results');
      }
      else {
        counterText = counterText.replace('%RESULTSTR%', 'result');
      }
      counterText = counterText.replace('%TERM%', this.currentSearchParams.q);

      this.$searchCounterEl.html(counterText);
    },
    createFilters: function () {
      var that = this;

      this.$filtersEl.html('');
      this.$filterHeaderEl.html('');
      if(this.resultTotalRecords > 0) {
        var activeFilters = [];
        var filterSectionTemplate = _.template($('#searchListFilterSectionTemplate').html());

        _.each(this.filtersTitles, function (filterTitle, key) {
          var filter = that.searchFilters[key];
          filter.filterTitle = filterTitle;
          if(filter.activeFilterItems.length > 0 || filter.inactiveFilterItems.length > 0) {
            activeFilters = activeFilters.concat(filter.activeFilterItems);

            that.$filtersEl.append(filterSectionTemplate(filter));
          }
        });

        $('.filterSection').each(function () {
          var items = $(this).find('ul:not(".active-filters") li');
          var $moreButton = $(this).find('[data-show-more-items]');
          if (items.length > 5) {
            that.showLessFilterItems($moreButton);
          }
          else {
            $moreButton.hide();
          }
        });

        this.createFilterHeader(activeFilters);
      }
    },
    createFilterHeader: function (activeFilters) {
      var filterHeaderTemplate = _.template($('#searchHeaderFilterTemplate').html());
      if(activeFilters.length) {
        this.$filterHeaderEl.append(filterHeaderTemplate({activeFilterItems: activeFilters}));
      }
    },
    getJsonFromUrl: function (hashBased) {
      var query;
      if(hashBased) {
        var pos = location.href.indexOf("?");
        if(pos==-1) return [];
        query = location.href.substr(pos+1);
      } else {
        query = location.search.substr(1);
      }
      var result = {};
      query.split("&").forEach(function(part) {
        if(!part) return;
        part = part.split("+").join(" "); // replace every + with space, regexp-free version
        var eq = part.indexOf("=");
        var key = eq>-1 ? part.substr(0,eq) : part;
        var val = eq>-1 ? decodeURIComponent(part.substr(eq+1)) : "";
        var from = key.indexOf("[");
        if(from==-1) {
          var key = decodeURIComponent(key);
          if(!_.isEmpty(result[key])) {
            if(_.isArray(result[key])) {
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
          var index = decodeURIComponent(key.substring(from+1,to));
          key = decodeURIComponent(key.substring(0,from));
          if(!result[key]) result[key] = [];
          if(!index) result[key].push(val);
          else result[key][index] = val;
        }
      });
      return result;
    }
  });

  new SearchResult();
})(jQuery);

