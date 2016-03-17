/*

  AlmQuery

  Usage reference:
  var query = AlmQuery.init();

  query.getMediaReferences(['10.1371/journal.pone.0144197', '10.1371/journal.pone.0151227'])
  .then(function (almData) {

  })
  .fail(function (error) {

  });

 */

var AlmQuery = {};

(function ($) {
  /*
   * Constructor function
   */

  AlmQuery.init = function (config) {
    this.config = config || ALM_CONFIG;
    return this;
  };

  /*
   * DOI format and validation functions
   */

  AlmQuery.isDOIValid = function (doi) {
    return !_.isEmpty(doi) && (_.isString(doi) || _.isArray(doi));
  };

  AlmQuery.formatDOI = function (doi) {
    doi = encodeURI(doi);
    return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
  };

  AlmQuery.validateDOI = function (doi) {
    var context = this;
    if(this.isDOIValid(doi)) {
      if(_.isString(doi)) {
        return this.formatDOI(doi);
      }
      else if (_.isArray(doi)) {
        return _.map(doi, function (value) { return context.formatDOI(value); });
      }
    }
    else {
      var e = new Error('[AlmQuery::validateDOI] - Invalid DOI');
      e.name = 'InvalidDOIError';
      throw e;
    }
  };

  /*
   * Request handling functions
   */

  AlmQuery.getRequestBaseUrl = function () {
    return this.config.host + '?api_key=' + this.config.apiKey;
  };

  AlmQuery.getRequestUrl = function (queryParams) {
    if(_.has(queryParams, 'ids')) {
      queryParams.ids = this.validateDOI(queryParams.ids);
    }

    queryParams = _.reduce(queryParams, function (memo, value, index) {
      if(_.isArray(value)) {
        value = _.reduce(value, function (memo, value) { return memo + value + ','; }, '');
        value = value.slice(0, -1);
      }

      return memo + '&' + index + '=' + value;
    }, '');

    return this.getRequestBaseUrl() + queryParams;
  };

  AlmQuery.processRequest = function (requestUrl) {
    var deferred = Q.defer();

    if(this.config.host == null) {
      var e = new Error('[AlmQuery::processRequest] - ALM API is not configured');
      e.name = 'ALMNotConfiguredError';
      deferred.reject(e);
    }
    else {
      $.ajax({
        url: requestUrl,
        jsonp: 'callback',
        dataType: 'jsonp',
        timeout: 20000,
        success: function (response) {
          deferred.resolve(response);
        },
        error: function (jqXHR, textStatus) {
          var e = new Error('[AlmQuery::processRequest] - Request failed to API');
          e.name = 'APIRequestError';
          deferred.reject(e);
        }
      });
    }

    return deferred.promise;
  };

  /*
  * API Request methods:
  */

  AlmQuery.getArticleSummary = function (doi) {
    var requestUrl = this.getRequestUrl({
      ids: doi
    });

    return this.processRequest(requestUrl);
  };

  AlmQuery.getArticleDetail = function (doi) {
    var requestUrl = this.getRequestUrl({
      ids: doi,
      info: 'detail'
    });

    return this.processRequest(requestUrl);
  };

  AlmQuery.getMediaReferences = function (doi) {
    var requestUrl = this.getRequestUrl({
      ids: doi,
      source_id: 'articlecoveragecurated',
      info: 'detail'
    });

    return this.processRequest(requestUrl);
  };

})(jQuery);