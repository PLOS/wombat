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
 * Querying the ALM API.
 */
(function ($) {

  function validateDOI(doi) {
    if (doi == null) {
      throw new Error('DOI is null.');
    }

    doi = encodeURI(doi);

    return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
  }

  /**
   * Query for one article's ALM summary.
   * @param doi       the article's DOI
   * @param successCallback  the callback to which to send the summary
   * @param errorCallback  the callback in which we handle errors
   */
  $.fn.getArticleSummary = function (doi, successCallback, errorCallback) {
    var config = ALM_CONFIG; // expect to import value from alm_config.js

    if (config.host == null) {
      // TODO: Replace with better console logging
      // console.log('ALM API is not configured');
      return;
    }

    doi = validateDOI(doi);
    var requestUrl = config.host + '?api_key=' + config.apiKey + '&ids=' + doi;

    $.ajax({
      url: requestUrl,
      jsonp: 'callback',
      dataType: 'jsonp',
      timeout: 20000,
      success: function (response) {
        if(response.data.length > 0) {
          successCallback(response.data[0]);
        }
      },
      error: function (jqXHR, textStatus) {
        // TODO: Replace with better console logging
        // console.log('ALM request failed: ' + requestUrl);
        errorCallback(textStatus);
      }
    })
  };

  $.fn.getMediaReferences = function (doi, successCallback, errorCallback) {
    var config = ALM_CONFIG; // expect to import value from alm_config.js

    if (config.host == null) {
      // TODO: Replace with better console logging
      // console.log('ALM API is not configured');
      return;
    }

    doi = validateDOI(doi);

    var requestUrl = config.host + '?api_key=' + config.apiKey + '&ids=' + doi + "&source_id=articlecoveragecurated&info=detail";

    //alert(requestUrl)
    //this.getData(request, callBack, errorCallback);

    $.ajax({
      url: requestUrl,
      jsonp: 'callback',
      dataType: 'jsonp',
      timeout: 20000,
      success: function (response) {
        successCallback(response.data[0]);
      },
      error: function (jqXHR, textStatus) {
        // TODO: Replace with better console logging
        // console.log('ALM request failed: ' + requestUrl);
        errorCallback(textStatus);
      }
    })
  };

  /*
   * TODO: Support querying for multiple article summaries in efficient batches.
   * See getArticleSummaries in legacy Ambra.
   */
})(jQuery);
