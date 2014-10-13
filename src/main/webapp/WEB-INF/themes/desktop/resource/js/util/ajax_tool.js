$.fn.getData = function (request, callback,) {
  var config =  ALM_CONFIG;

 // var url = this.almHost + '?api_key=' + this.almAPIKey + '&ids=' + request;
  var url = config.host + '?api_key=' + config.apiKey + '&ids=' + request;
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
  })
  console.log(url);

};