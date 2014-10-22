1. do the ajax; how get data from the ajax?







var displayTweetsArticleSidebar = function (doi) {
  doi = validateDOI(doi);
  var request, config, requestUrl, response;
  request = doi + '&source=twitter&info=event';

  config = ALM_CONFIG;

  requestUrl = config.twitterhost +'?api_key=' + config.apiKey + '&ids=' + request;
  response = '';
  $.ajax({
    type: "GET",
    url: requestUrl,
    json: 'callback',
    dataType: 'json',
    success: function(responseText) {
      response = responseText[0].sources[0];

      return (response);
    },
    failure: function () {
      // console.log('ALM request failed: ' + errorText+ " "+ url);
      $('#twitter-container').append(errorText);
    }
  });

};