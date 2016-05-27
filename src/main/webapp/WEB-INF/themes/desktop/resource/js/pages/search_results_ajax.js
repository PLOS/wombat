var SearchResult;

(function ($) {
  SearchResult = Class.extend({
    isInitialized: false,

    $resultListEl: $(),
    $filtersEl: $(),
    $searchBarForm: $('#searchControlBarForm'),
    $searchBarInput: $('#controlBarSearch'),
    $loadingEl: $('#search-loading'),
    $orderByEl: $('#sortOrder'),
    $resultPerPageEl: $('#resultsPerPageDropdown'),

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

    searchFilters: {},
    results: [],
    resultTotalRecords: 0,
    resultsOffset: 0,
    pagination: null,

    init: function () {
      var that = this;
      this.loadUrlParams();
      this.bindSearchEvents();
      this.pagination = new Pagination(this.getCurrentPage(), this.resultTotalRecords, this.getResultsPerPage(), function (currentPage) {
        that.currentSearchParams.page = currentPage;
        that.processRequest();
      });

      this.processRequest();
    },
    getCurrentPage: function() {
      return parseInt(this.currentSearchParams.page);
    },
    getResultsPerPage: function () {
      return parseInt(this.currentSearchParams.resultsPerPage);
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
        if(!_.isEmpty(item)) {
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

        that.processRequest();
      });

      this.$orderByEl.on('change', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.currentSearchParams.sortOrder = $(this).val();
        that.processRequest();
      });

      this.$resultPerPageEl.on('change', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.currentSearchParams.resultsPerPage = $(this).val();
        that.processRequest();
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
          that.resultTotalRecords = response.searchResults.numFound;
          if(that.resultTotalRecords > 0) {
            that.results = response.searchResults.docs;
            that.pagination.setItemsPerPage(that.getResultsPerPage());
            that.pagination.setTotalRecords(that.resultTotalRecords);
            that.resultsOffset = response.searchResults.start;

            that.searchFilters = response.searchFilters;

            that.createFilters();
            that.createResultList();
            var pagination = that.pagination.getPaginationElement();

            that.hideLoading();
          }
          else {
            that.showNoResults();
          }
        },
        error: function (jqXHR, textStatus) {
          that.showRequestError();
        }
      });

    },
    showRequestError: function () {
      
    },
    showNoResults: function() {

    },
    createResultList: function () {

    },
    createFilters: function () {

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

