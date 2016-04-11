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
