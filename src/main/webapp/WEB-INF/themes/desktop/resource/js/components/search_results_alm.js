  function handleUndefinedOrZeroMetric(metric) {
    return metric == undefined || metric == 0 ? 'None' : metric;
  }

  function appendOrRemoveLink(link, metric) {
    metric = handleUndefinedOrZeroMetric(metric);
    if (metric == 'None') {
      link.html(link.html() + ' ' + metric);
      link.contents().unwrap();
    } else {
      link.append(metric);
    }
  }

  $(document).ready(function () {

    $('.search-results-alm').each(function () {
      var $alm = $(this);
      var doi = $alm.data('doi');
      $alm.getArticleSummary(doi, function (almData) { //success function

        var almLinks = $alm.find('a');
        var viewsLink = $(almLinks[0]);
        appendOrRemoveLink(viewsLink, almData.viewed);

        var citationsLink = $(almLinks[1]);
        appendOrRemoveLink(citationsLink, almData.cited);

        var savesLink = $(almLinks[2]);
        appendOrRemoveLink(savesLink, almData.saved);

        var sharesLink = $(almLinks[3]);
        appendOrRemoveLink(sharesLink, almData.shared);

        $alm.siblings('.search-results-alm-loading').fadeOut('slow', function () {
          $alm.fadeIn();
        })
      }, function () { //error function
        $alm.siblings('.search-results-alm-loading').fadeOut('slow', function () {
          $alm.siblings('.search-results-alm-error').fadeIn();
        })
      });
    });
  });