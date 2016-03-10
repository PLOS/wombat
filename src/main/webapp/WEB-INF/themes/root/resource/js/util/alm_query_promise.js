var AlmQuery = {}

(function ($) {

  AlmQuery.init = function (config) {
    this.config = config || ALM_CONFIG;

    var deferred = Q.defer();

    if(this.config.host == null) {
      deferred.reject(new Error('ALM API is not configured'));
    }
    else {
      deferred.resolve();
    }

    return deferred.promise;

  };

  AlmQuery.getRequestBaseUrl = function () {
    return this.config.host + '?api_key=' + this.config.apiKey;
  };

  AlmQuery.getRequestUrl = function (queryParams) {
    var queryParams = _.reduce(queryParams, function (memo, value, index) {
      if(_.isArray(value)) {
        value = _.reduce(value, function (memo, value) { return memo + value + ','; }, '');
        value = value.slice(0, -1);
      }

      return memo + '&' + index + '='+value;
    }, '');

    return this.getRequestBaseUrl() + queryParams;
  };

  AlmQuery.getArticleSummary = function (doi) {

  };

})(jQuery);