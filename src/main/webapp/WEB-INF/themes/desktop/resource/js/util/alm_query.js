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
   * @param callback  the callback to which to send the summary
   */
  $.fn.getArticleSummary = function (doi, callback) {
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
      success: function (response) {
        callback(response.data[0]);
      },
      failure: function () {
        // TODO: Replace with better console logging
        // console.log('ALM request failed: ' + requestUrl);
      }
    })
  };

  /*
   * TODO: Support querying for multiple article summaries in efficient batches.
   * See getArticleSummaries in legacy Ambra.
   */
})(jQuery);
