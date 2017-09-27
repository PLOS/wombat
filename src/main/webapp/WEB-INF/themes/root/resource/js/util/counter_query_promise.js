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

  CounterQuery

  Usage reference:
  var query = new CounterQuery();

  query.getArticleSummary('10.1371/journal.pone.0144197')
  .then(function (counterData) {

  })
  .fail(function (error) {

  });

 */

var CounterQueryValidator;
var CounterQuery;


(function ($) {

  CounterQueryValidator = Class.extend({

    promise: null,

    init: function (options) {
      var deferred = Q.defer();
      var defaultOptions = {};
      this.options = _.extend(defaultOptions, options);
      deferred.resolve(true);
      this.promise = deferred.promise;
    },

    validate: function (data) {
      // todo: implement actual response validation
      return this.promise
        .then(function () {
          return data;
        });
    }

  });

  CounterQuery = Class.extend({
    /*
     * Constructor function
     */

    init: function (config) {

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
        throw new ErrorFactory('InvalidDOIError', '[CounterQuery::validateDOI] - Invalid DOI');
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
    * Validator used for all the instance's queries. Defaults to CounterQueryValidator.
    */
    dataValidator: new CounterQueryValidator(),

    /*
     * Set a custom dataValidator, should be a CounterQueryValidator or child class instance.
     */
    setDataValidator: function (customValidator) {
      if(customValidator instanceof CounterQueryValidator) {
        this.dataValidator = customValidator;
      }

      return this;
    },

    
    /*
     * Request handling functions
     */


    getRequestUrl: function (doi) {
      return COUNTER_HOST + '/' + doi;
    },

    processRequest: function (requestUrl) {
      var that = this;
      var deferred = Q.defer();

      if(COUNTER_HOST == null) {
        var err = new Error('[CounterQuery::processRequest] - COUNTER API is not configured');
        err.name = 'COUNTERNotConfiguredError';
        deferred.reject(new ErrorFactory('COUNTERNotConfiguredError', '[CounterQuery::processRequest] - COUNTER API is not configured'));

      }
      else {
        $.ajax({
          url: requestUrl,
          jsonp: 'callback',
          dataType: 'json',
          timeout: 60000,
          success: function (response) {
            that.dataValidator.validate(response)
              .then(function (data) {
                deferred.resolve(data);
              })
              .fail(function (error) {
                if(that.isArticleNew()) {
                  deferred.reject(new ErrorFactory('NewArticleError', '[CounterQuery::processRequest] - The article is too new to have data.'));
                }
                else {
                  deferred.reject(error);
                }
              });

          },
          error: function (jqXHR, textStatus) {
            var err = new Error('[CounterQuery::processRequest] - Request failed to API');
            err.name = 'APIRequestError';
            deferred.reject(new ErrorFactory('APIRequestError', '[CounterQuery::processRequest] - Request failed to API'));
          }
        });
      }

      return deferred.promise;
    },

    /*
     * API Request methods:
     */

    getArticleSummary: function (doi) {
      var requestUrl = this.getRequestUrl(doi);
      return this.processRequest(requestUrl);
    }
  });

})(jQuery);