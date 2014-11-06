
(function ($) {
  $.fn.signposts = function (doi) {
    var errorText, tooSoonText, initData, issued, compareDate, todaysDate,
      views, saves, shares, citations, listBody;

    function validateDOI(doi) {
      if (doi == null) {
        throw new Error('DOI is null.');
      }

      doi = encodeURI(doi);

      return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
    }

      function formatNumberComma(num) {
        var fixNum = num.toString()
        fixNum = fixNum.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        return fixNum;
      };


    this.getSignpostData = function (doi) {
      doi = validateDOI(doi);
      var config, requestUrl, errorText;

      config = ALM_CONFIG;

      requestUrl = config.host + '?api_key=' + config.apiKey + '&ids=' + doi + '&info=detail';
      console.log(requestUrl);
      errorText = '<li class="alm-error">Article metrics are unavailable at this time. Please try again later.</li>';


      $.ajax({
        url: requestUrl,
        dataType: 'jsonp',
        contentType: "text/json; charset=utf-8",
        type: "GET"
      }).done(function (data) {
        initData = data.data[0];


        if (initData === undefined) {
          $('#almSignposts').append(errorText);

        } else {
          issued = data.data[0].issued["date-parts"];
          compareDate = new Date(issued);
          todaysDate = new Date();
console.log(todaysDate.getDate() - 3);
          console.log(compareDate.getDate());
          if (todaysDate - compareDate < 4) {
            tooSoonText = '<li></li><li></li><li class="too-soon">Article metrics are unavailable up to 3 days after publication</li>';
            $('#almSignposts').append(tooSoonText);

          } else {

            views = formatNumberComma(data.data[0].viewed);
            saves = formatNumberComma(data.data[0].saved);
            shares = formatNumberComma(data.data[0].discussed);
            citations = formatNumberComma(data.data[0].cited);

            listBody = '<li>' + saves + '<span>Saves</span></li>' +
              '<li>' + citations + '<span>Citations</span></li>' +
              '<li>' + views + '<span>Views</span></li>' +
              '<li>' + shares + '<span>Shares</span></li>';
            $('#almSignposts').append(listBody);

          }
        }
      }).fail(function () {
        $('#almSignposts').append(errorText);
      });

    }
  }
})(jQuery);