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

var SearchResultsCounterData;

(function ($) {

  SearchResultsCounterData = Class.extend({

    containerID: '.search-results-alm-container',
    DOIlist: [],

    init: function (DOIlist) {
      this.setDOIList(DOIlist);
    },

    setDOIList: function (DOIlist) {
      this.DOIlist = DOIlist;
    },
    getDOIList: function () {
      return this.DOIlist;
    },
    processCounterDataRequest: function () {
      var that = this;

      counterQueries = [];
      _.each(that.DOIlist, function(doi) {
        var counterQuery = new CounterQuery();
        var counterValidator = new CounterQueryValidator();
        counterQuery
          .setDataValidator(counterValidator)
          .getArticleSummary(doi)
          .then(function (counterData) {
            that.showCounterData(doi, counterData);
          })
          .fail(function (error) {
            that.showALMErrorMessage(error);
          });
        counterQueries.push(counterQuery);
      });

    },
    showCounterData: function (doi, data) {
      var that = this;
      var template = _.template($('#searchResultsAlm').html());
      var templateData = {
        itemDoi: doi,
        saveCount: 0,
        citationCount: 0,
        shareCount: 0,
        viewCount: data['get-document']
      };

      $(that.containerID).filter("[data-doi='" + doi + "']").html(template(templateData));

    },
    showALMErrorMessage: function (error) {
      var template = _.template($('#searchResultsAlmError').html());
      $(this.containerID).html(template());
    }
  });


})(jQuery);