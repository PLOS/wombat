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

var SearchResultsALMData;

(function ($) {

  SearchResultsALMData = Class.extend({

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
    processALMDataRequest: function () {
      var that = this;

      var query = new AlmQuery();
      var validator = new AlmQueryValidator({checkSources: false});

      query
          .setDataValidator(validator)
          .getArticleSummary(that.DOIlist)
          .then(function (data) {
            that.showALMData(data)
          })
          .fail(function (error) {
            that.showALMErrorMessage();
          });
    },
    showALMData: function (data) {
      var that = this;
      var template = _.template($('#searchResultsAlm').html());
      _.each(data, function (i) {
        var doi = i.doi;
        var saved_count = _.findWhere(i.sources, {name: 'mendeley'}).metrics.total;
        var templateData = {
          itemDoi: doi,
          saveCount: saved_count,
          citationCount: i.cited,
          shareCount: i.discussed,
          viewCount: i.viewed
        };

        $(that.containerID).filter("[data-doi='" + doi + "']").html(template(templateData));

      });
    },
    showALMErrorMessage: function (error) {
      var template = _.template($('#searchResultsAlmError').html());
      $(this.containerID).html(template());
    }
  });


})(jQuery);
