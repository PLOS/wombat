(function ($) {
  $.fn.signposts = function (doi) {
    var errorText, tooSoonText, initData, issued, date_check, compareDate, isThree, plural_check, views, saves, shares, citations, listBody;

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

    date_check = function (logDate, numDays) {
      ///requires moment.js
      var testDate = new Date().addDays(numDays),
          newFormat = "YYYYMMDD",
          testDateFormat = moment(testDate).format(newFormat),
          logDateFormat = moment(logDate).format(newFormat);

      if (logDateFormat > testDateFormat) {
        return false;

      } else {
        // The selected time is more than numDays days from now
        return true;
      }
    };

    plural_check = function (input) {
      input = parseInt(input.replace(/[^0-9]/g, ''));
      if (input === 1) {
        return false;
      } else {
        return true;
      }
    };

    this.getSignpostData = function (doi) {
      doi = validateDOI(doi);
      var config, requestUrl, errorText, tooSoonText, pubDate, offsetDays;

      pubDate = $('meta[name=citation_date]').attr("content");

      offsetDays = 3; // TODO: if this number is one then add some logic to make it days singular - not an issue now.
      tooSoonText = '<li></li><li></li><li id="tooSoon">Article metrics are unavailable up to ' + offsetDays + '  days after publication</li>';
      errorText = '<li id="metricsError">Article metrics are unavailable at this time. Please try again later.</li>';

      config = ALM_CONFIG;

      requestUrl = config.host + '?api_key=' + config.apiKey + '&ids=' + doi + '&info=detail';

      function displayError(message) {
        $('#almSignposts').html(message);
        $('#loadingMetrics').css('display', 'none');
      }

      $.ajax({
        url:         requestUrl,
        dataType:    'jsonp',
        contentType: "text/json; charset=utf-8",
        type:        "GET",
        timeout:     20000
      }).done(function (data) {
        initData = data.data[0];
        if (initData === undefined) {
          // is date less than "offsetDays" number of  days ago

          numberOfDays = date_check(pubDate, offsetDays);

          if (numberOfDays === true) {
            displayError(tooSoonText);
          } else {
            displayError(errorText);
          }
        } else {

          //get the numbers & add commas where needed
          saves = formatNumberComma(data.data[0].saved);
          citations = formatNumberComma(data.data[0].cited);
          views = formatNumberComma(data.data[0].viewed);
          shares = formatNumberComma(data.data[0].discussed);

          //check if term needs to be plural
          function build_parts(li_id, metric) {
            var plural = plural_check(metric);
            if (plural === true) {
              $(li_id).prepend(metric).find('.metric-term').append('s');
            } else {
              $(li_id).prepend(metric);
            }
          }

          build_parts('#almSaves', saves);
          build_parts('#almCitations', citations);
          build_parts('#almViews', views);
          build_parts('#almShares', shares);

          var scopus = data.data[0].sources[4].metrics.total;
          if (scopus > 0) {
            $('#almCitations').find('.citations-tip a').html('Scopus data unavailable. Displaying Crossref citation count.');
          } else {
            //
          }

          $('#loadingMetrics').css('display', 'none');

          $('#almSignposts li').removeClass('noshow');

//          }
        }
      }).fail(function () {
        displayError(errorText);
      });

    }
  };

  $('.metric-term').mouseenter(function () {

    clearTimeout($(this).data('mouseId'));
    $(this).addClass('show-tip');
    var tippy = $(this).next();

    $(tippy).fadeIn('fast').addClass('tippy');

  }).mouseleave(function () {
    var boxtop = $(this);
    var tippy = $(this).next();

    $(tippy).mouseenter(function () {

      var boxtop = $(tippy).prev();
      clearTimeout($(boxtop).data('mouseId'));
      if ($(boxtop).hasClass('show-tip')) {} else {$(boxtop).addClass('show-tip');}

    }).mouseleave(function () {
      var boxtop = $(tippy).prev();

      $(boxtop).removeClass('show-tip');

      $(tippy).fadeOut('fast');
    });

    var mouseId = setTimeout(function () {

      $(tippy).fadeOut('fast');

      $(boxtop).removeClass('show-tip');
    }, 250);

    $(boxtop).data('mouseId', mouseId);
  });

})(jQuery);


