
(function ($) {
  $.fn.signposts = function (doi) {
    var errorText, tooSoonText, initData, issued, date_check, compareDate, isThree, plural_check, isPlural,
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

    Date.prototype.addDays = function (days) {
      this.setDate(this.getDate() + days);
      return this;
    };

    date_check = function (input, numDays) {
      var inputDate = new Date(input);
      var dateWeek = new Date().addDays(numDays);
      console.log(inputDate, dateWeek);
      if (inputDate < dateWeek) {
        return false;

      } else {
        // The selected time is more than numDays days from now
        return true;
      }
    }
    plural_check = function(input){
      if (input === 1){
        return false;
      } else {
        +'s';
      }
    }

    this.getSignpostData = function (doi) {
      doi = validateDOI(doi);
      var config, requestUrl, errorText;

      config = ALM_CONFIG;

      requestUrl = config.host + '?api_key=' + config.apiKey + '&ids=' + doi + '&info=detail';
      console.log(requestUrl);
      errorText = '<li id="metricsError">Article metrics are unavailable at this time. Please try again later.</li>';

      $.ajax({
        url: requestUrl,
        dataType: 'jsonp',
        contentType: "text/json; charset=utf-8",
        type: "GET"
      }).done(function (data) {
        initData = data.data[0];

        if (initData === undefined) {
          $('#almSignposts').append(errorText);

        } else { // is date less than 3 days ago
          issued = data.data[0].issued["date-parts"];
          compareDate = new Date(issued);
          isThree = date_check(compareDate, 3);

          if (isThree === true) {
            tooSoonText = '<li></li><li></li><li id="tooSoon">Article metrics are unavailable up to 3 days after publication</li>';
            $('#almSignposts').append(tooSoonText);

          } else {

            views = formatNumberComma(data.data[0].viewed);
            saves = formatNumberComma(data.data[0].saved);
            shares = formatNumberComma(data.data[0].discussed);
            citations = formatNumberComma(data.data[0].cited);


            $('#almViews').append(views)
           /* listBody = '<li>' + saves + '<span>Saves</span></li>' +
              '<li>' + citations + '<span>Citations</span></li>' +
              '<li>' + views + '<span>Views</span></li>' +
              '<li>' + shares + '<span>Shares</span></li>';*/
            $('#loadingMetrics').css('display','none');
            $('#almSignposts').append(listBody);

          }
        }
      }).fail(function () {
        $('#almSignposts').append(errorText);
      });

    }
  }
})(jQuery);