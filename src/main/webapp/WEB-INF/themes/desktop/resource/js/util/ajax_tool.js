$.fn.getData = function (request, callback, errorCallback) {
  var config =  ALM_CONFIG;

  var url = config.host + '?api_key=' + config.apiKey + '&ids=' + request;

  $.ajax({
    url: url,
    jsonp: 'callback',
    dataType: 'jsonp',
    success: function (response) {
      callback(response.data[0]);
    },
    failure: function () {
      // TODO: Replace with better console logging
      console.log('ALM request failed: ' + requestUrl);
    }
  });
  /*
   $.jsonp({
   url: url,
   context: document.body,
   timeout: 20000,
   callbackParameter: "callback",
   success: callBack,
   error: function (xOptions, msg) {
   errorCallback("Our system is having a bad day. We are working on it. Please check back later.")
   }
   });*/
  console.log(url);

};