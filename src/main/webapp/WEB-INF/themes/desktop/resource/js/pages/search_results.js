(function ($) {

  $.fn.displayAlmSummary = function (doi, index) {
    $(this).getArticleSummary(doi, function (almData) { //success function
      var $alm = $('#search-results-alm-' + index);

      var almLinks = $alm.find('a');
      var viewsLink = $(almLinks[0]);
      appendOrRemoveLink(viewsLink, almData.viewed);

      var citationsLink = $(almLinks[1]);
      appendOrRemoveLink(citationsLink, almData.cited);

      var savesLink = $(almLinks[2]);
      appendOrRemoveLink(savesLink, almData.saved);

      var sharesLink = $(almLinks[3]);
      appendOrRemoveLink(sharesLink, almData.shared);

      $('.search-results-alm-loading').fadeOut('slow', function() {
        $('.search-results-alm').fadeIn();
      })
    }, function() { //error function
      $('.search-results-alm-loading').fadeOut('slow', function() {
        $('.search-results-alm-error').fadeIn();
      })
    });
  };

  function handleUndefinedMetric(metric) {
    return metric == undefined ? 'None' : metric;
  }

  function appendOrRemoveLink(link, metric) {
    metric = handleUndefinedMetric(metric);
    if (metric == 'None') {
      link.html(link.html() + ' ' + metric);
      link.contents().unwrap();
    } else {
      link.append(metric);
    }
  }

  $(document).ready(function() {
    $('#sortOrder').on('change', function() {
      $(this).parents('form').submit();
    });
  });

})(jQuery);