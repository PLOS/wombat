  /*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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