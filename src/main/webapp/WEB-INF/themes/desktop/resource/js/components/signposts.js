
(function ($) {
  $.fn.signposts = function (doi) {
    var errorText, tooSoonText, initData, issued, date_check, compareDate, isThree, plural_check,
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

    date_check = function (logDate, numDays) {

      var testDate = new Date().addDays(numDays);

      if (logDate < testDate) {
        return false;

      } else {
        // The selected time is more than numDays days from now
        return true;
      }
    };

    plural_check = function(input){
      if (input == 1){
        //do nothing
      } else {
        return true;
      }
    };

    this.getSignpostData = function (doi) {
      doi = validateDOI(doi);
      var config, requestUrl, errorText;

      config = ALM_CONFIG;

      requestUrl = config.host + '?api_key=' + config.apiKey + '&ids=' + doi + '&info=detail';

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
        // is date less than 3 days ago
          issued = data.data[0].issued["date-parts"];

          compareDate = moment(issued, "YYYY,MM,DD");

          isThree = date_check(compareDate, 3);

          if (isThree === true) {
            tooSoonText = '<li></li><li></li><li id="tooSoon">Article metrics are unavailable up to 3 days after publication</li>';
            $('#almSignposts').html(tooSoonText);
            $('#loadingMetrics').css('display','none');
          } else {
            //get the numbers & add commas where needed
            saves = formatNumberComma(data.data[0].saved);
            citations = formatNumberComma(data.data[0].cited);
            views = formatNumberComma(data.data[0].viewed);
            shares = formatNumberComma(data.data[0].discussed);

            //check if term needs to be plural
            function build_parts(li_id, metric){
              var plural = plural_check(metric);
              if(plural === true) {
                $(li_id).prepend(metric).find('.metric-term').append('s');
              } else {
                $(li_id).prepend(metric);
              }
            }
            build_parts('#almSaves',saves);
            build_parts('#almCitations',citations);
            build_parts('#almViews',views);
            build_parts('#almShares', shares);

            var scopus = data.data[0].sources[4].metrics.total;
            if (scopus > 0){
              $('#almCitations').find('.citations-tip a').html('Scopus data unavailable. Displaying Crossref citation count.');
            } else {
              //
            }

            $('#loadingMetrics').css('display','none');

            $('#almSignposts li').removeClass('noshow');

          }
        }
      }).fail(function () {
        errorText = '<li id="metricsError">Article metrics are unavailable at this time. Please try again later.</li>';
        $('#loadingMetrics').css('display','none');
        $('#almSignposts').html(errorText);
      });

    }
  };

  $('.metric-term').mouseenter(function () {

    clearTimeout($(this).data('mouseId'));
    $(this).addClass('show-tip');
    var tippy = $(this).next();

    $(tippy).fadeIn('fast').addClass('tippy');

  }).mouseleave(function (){
    var boxtop = $(this);
    var tippy = $(this).next();

      $(tippy).mouseenter(function(){

      var boxtop = $(tippy).prev();
        clearTimeout($(boxtop).data('mouseId'));
        if($(boxtop).hasClass('show-tip')){} else {$(boxtop).addClass('show-tip');}

      }).mouseleave(function () {
        var boxtop = $(tippy).prev();

        $(boxtop).removeClass('show-tip');

        $(tippy).fadeOut('fast');
        });

    var mouseId = setTimeout(function(){

        $(tippy).fadeOut('fast');

      $(boxtop).removeClass('show-tip');
    }, 250);

      $(boxtop).data('mouseId', mouseId);
  });

})(jQuery);


