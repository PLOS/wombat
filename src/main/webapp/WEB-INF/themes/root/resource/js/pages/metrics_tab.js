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

var MetricsTab = {};

(function ($) {

  MetricsTab.components = [(new MetricsViewedSection()), (new MetricsCitedSection()), (new MetricsSavedSection()), (new MetricsDiscussedSection()), (new MetricsRecommendedSection())];

  MetricsTab.getComponents = function () {
    return this.components;
  };
  MetricsTab.init = function () {
    var that = this;


    var counter_query = new CounterQuery();
    var counter_validator = new CounterQueryValidator();

    counter_query
      .setDataValidator(counter_validator)
      .getArticleSummary(ArticleData.doi)
      .then(function (counterData) {
        combineWithAlmRequest(counterData);
      })
      .fail(function (error) {
        show_error(error, that);
      });

    function combineWithAlmRequest(counterData) {
      var query = new AlmQuery();
      query
        .getArticleDetail(ArticleData.doi)
        .then(function (articleData) {
          var data = articleData[0];
          data.counterData = counterData;
          _.each(that.getComponents(), function (value) {
            value.loadData(data);
          });
        })
        .fail(function (error) {
          show_error(error, that);
        })
    }

  };

  function show_error(error, that) {
    switch (error.name) {
      case 'NewArticleError':
        _.each(that.getComponents(), function (value) {
          value.newArticleError();
        });
        break;
      default:
        _.each(that.getComponents(), function (value) {
          value.dataError();
        });
        break;
    }
  }

  MetricsTab.init();

})(jQuery);