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

/*

  AlmQuery

  Usage reference:
  var query = new AlmQuery();

  query.getMediaReferences(['10.1371/journal.pone.0144197', '10.1371/journal.pone.0151227'])
  .then(function (almData) {

  })
  .fail(function (error) {

  });

 */

var AlmQueryValidator;
var AlmQuery;


(function ($) {

  AlmQueryValidator = Class.extend({

    promise: null,

    init: function (options) {
      var deferred = Q.defer();
      var defaultOptions = {
          checkSources: true,
          excludeInvalidSources: true,
          checkIsUndefined: true,
          excludeUndefinedItems: true,
          checkHasData: true
        };
      this.options = _.extend(defaultOptions, options);
      deferred.resolve(true);
      this.promise = deferred.promise;
    },

    validate: function (data) {
      var that = this;

      return this.promise
        .then(function () {
          if(that.options.checkHasData) {
            if(_.has(data, 'data') && data.data.length) {
              return data.data;
            }
            else {
              throw new ErrorFactory('AlmQueryValidatorError', '[AlmQueryValidator::validate] - Data has no "data" key');
            }
          }

          return data;
        })
        .then(function (data) {
          if(that.options.checkIsUndefined) {
            var undefinedDataError = new ErrorFactory('AlmQueryValidatorError', '[AlmQueryValidator::validate] - Data is undefined');
            if(_.isArray(data) && data.length) {
              if(that.options.excludeUndefinedItems) {
                var newData = [];
                _.each(data, function (item) {
                  if(!_.isUndefined(item)) {
                    newData.push(item);
                  }
                });

                if(!newData.length) {
                  throw undefinedDataError;
                }

                data = newData;
              }
              else {
                _.each(data, function (item) {
                  if(_.isUndefined(item)) {
                    throw undefinedDataError;
                  }
                });
              }
            }
            else if(_.isUndefined(data)) {
              throw undefinedDataError;
            }
          }

          return data;
        })
        .then(function (data) {
          if(that.options.checkSources) {
            var invalidSourceError = new ErrorFactory('AlmQueryValidatorError', '[AlmQueryValidator::validate] - Data source is invalid');
            if(_.isArray(data) && data.length) {
              if(that.options.excludeInvalidSources) {
                var newData = [];
                _.each(data, function (item) {
                  if(that.validateSource(item)) {
                    newData.push(item);
                  }
                });

                if(!newData.length) {
                  throw invalidSourceError;
                }

                data = newData;
              }
              else {
                _.each(data, function (item) {
                  if(!that.validateSource(item)) {
                    throw invalidSourceError;
                  }
                });
              }
            }
            else if(that.validateSource(data)) {
              throw invalidSourceError;
            }
          }

          return data;
        });
    },

    validateSource: function (item) {
      return _.has(item, 'sources') && !_.isUndefined(item.sources);
    }

  });

  AlmQuery = Class.extend({
    /*
     * Constructor function
     */

    init: function (config) {
      this.config = _.extend(ALM_CONFIG, config);
      this.hasSessionStorage = Modernizr.sessionstorage;
    },
    
    /*
     * DOI format and validation functions
     */

    isDOIValid: function (doi) {
      return !_.isEmpty(doi) && (_.isString(doi) || _.isArray(doi));
    },

    formatDOI: function (doi) {
      doi = encodeURI(doi);
      return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
    },

    validateDOI: function (doi) {
      var that = this;
      if(this.isDOIValid(doi)) {
        if(_.isString(doi)) {
          return this.formatDOI(doi);
        }
        else if (_.isArray(doi)) {
          return _.map(doi, function (value) { return that.formatDOI(value); });
        }
      }
      else {
        throw new ErrorFactory('InvalidDOIError', '[AlmQuery::validateDOI] - Invalid DOI');
      }
    },

    isArticleNew: function () {
      var publishDate = moment(ArticleData.date, "MMM DD, YYYY").valueOf();
      var todayMinus48Hours = moment().subtract(2, 'days').valueOf();
      return (todayMinus48Hours < publishDate);
    },

    /*
    * Data validation functions:
    */

    /*
    * Validator used for all the instance's queries. Defaults to AlmQueryValidator.
    */
    dataValidator: new AlmQueryValidator(),

    /*
     * Set a custom dataValidator, should be a AlmQueryValidator or child class instance.
     */
    setDataValidator: function (customValidator) {
      if(customValidator instanceof AlmQueryValidator) {
        this.dataValidator = customValidator;
      }

      return this;
    },

    
    /*
     * Request handling functions
     */

    getRequestBaseUrl: function () {
      return this.config.host + '?api_key=' + this.config.apiKey;
    },

    getRequestUrl: function (queryParams) {
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
    },

    processRequest: function (requestUrl) {
      var that = this;
      var deferred = Q.defer();

      if(this.config.host == null) {
        var err = new Error('[AlmQuery::processRequest] - ALM API is not configured');
        err.name = 'ALMNotConfiguredError';
        deferred.reject(new ErrorFactory('ALMNotConfiguredError', '[AlmQuery::processRequest] - ALM API is not configured'));

      }
      else {
        $.ajax({
          url: requestUrl,
          jsonp: 'callback',
          dataType: 'jsonp',
          timeout: 60000,
          success: function (response) {
            that.dataValidator.validate(response)
              .then(function (data) {
                deferred.resolve(data);
              })
              .fail(function (error) {
                if(that.isArticleNew()) {
                  deferred.reject(new ErrorFactory('NewArticleError', '[AlmQuery::processRequest] - The article is too new to have data.'));
                }
                else {
                  deferred.reject(error);
                }
              });

          },
          error: function (jqXHR, textStatus) {
            var err = new Error('[AlmQuery::processRequest] - Request failed to API');
            err.name = 'APIRequestError';
            deferred.reject(new ErrorFactory('APIRequestError', '[AlmQuery::processRequest] - Request failed to API'));
          }
        });
      }

      return deferred.promise;
    },

    /*
     * API Request methods:
     */

    getArticleSummary: function (doi) {
      var requestUrl = this.getRequestUrl({
        ids: doi
      });

      return this.processRequest(requestUrl);
    },

    getArticleDetail: function (doi) {
      var requestUrl = this.getRequestUrl({
        ids: doi,
        info: 'detail'
      });

      return this.processRequest(requestUrl);
    },

    getMediaReferences: function (doi) {
      var requestUrl = this.getRequestUrl({
        ids: doi,
        source_id: 'articlecoveragecurated',
        info: 'detail'
      });

      return this.processRequest(requestUrl);
    },
    getArticleTweets: function (doi) {
      var requestUrl = this.getRequestUrl({
        ids: doi,
        source_id: 'twitter',
        info: 'detail'
      });

      return this.processRequest(requestUrl);
    }
  });

})(jQuery);