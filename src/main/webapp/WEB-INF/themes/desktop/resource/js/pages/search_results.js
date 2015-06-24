(function ($) {

  $.fn.displayALMSummary = function (doi, index) {
    $(this).getArticleSummary(doi, function (almData) {
      var $alm = $('#search-results-alm-' + index);

      var viewsLink = $alm.find('a').first();
      appendOrRemoveLink(viewsLink, almData.viewed);

      var citationsLink = $alm.find('a:nth-child(2)');
      appendOrRemoveLink(citationsLink, almData.cited);

      var savesLink = $alm.find('a:nth-child(3)');
      appendOrRemoveLink(savesLink, almData.saved);

      var sharesLink = $alm.find('a').last();
      appendOrRemoveLink(sharesLink, almData.shared);
    });
    $('.search-results-alm-loading').fadeOut(function() {
      $('.search-results-alm').fadeIn();
    })
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

})(jQuery);