(function ($) {

  $.fn.displayALMSummary = function (doi, index) {
    $(this).getArticleSummary(doi, function (almData) {
      var $alm = $('#search-results-alm-' + index);
      var viewsLink = $alm.find('a').first();
      viewsLink.append(handleUndefinedMetric(almData.viewed));
      var citationsLink = $alm.find('a:nth-child(2)');
      citationsLink.append(handleUndefinedMetric(almData.cited));
      var savesLink = $alm.find('a:nth-child(3)');
      savesLink.append(handleUndefinedMetric(almData.saved));
      var sharesLink = $alm.find('a').last();

      var shared = handleUndefinedMetric(almData.shared);
      if (shared == 'None') { //todo: methodize this somehow
        sharesLink.html('Shares: None');
        sharesLink.contents().unwrap();
      } else {
        sharesLink.append(shared);
      }
    });
  };

  function handleUndefinedMetric(metric) {
    return metric == undefined ? 'None' : metric;
  }
})(jQuery);