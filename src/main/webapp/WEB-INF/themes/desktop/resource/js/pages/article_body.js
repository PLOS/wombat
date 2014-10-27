(function ($) {

  function getMediaCoverageCount(almData) {
    for (var i = 0; i < almData.sources.length; i++) {
      if (almData.sources[i].name == 'articlecoveragecurated') {
        return almData.sources[i].metrics.total;
      }
    }
    return 0;
  }

  function addMediaCoverageLink() {
    var $media = $('#nav-media');
    if ($media.length) {
      var doi = $media.data('doi');
      $media.getArticleSummary(doi,
          function (data) {
            var mediaCoverageCount = getMediaCoverageCount(data);
            $media.find('#media-coverage-count').text('(' + mediaCoverageCount + ')');
          });
    }
  }

  /// write article nav
  $article = $('.article-content');

  $('#nav-article').buildNav({
    content: $article
  });

  $('#nav-article').floatingNav({
    sections: $article.find('div.toc-section')
  });

  addMediaCoverageLink();


  /// build figure carousel
  $('#figure-carousel').buildFigureCarousel();

  function formatHumanReadableByteSize(bytes) {
    var suffices = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
    var increment = 1000; // could change to 1024
    var precision = 100;

    var n = bytes;
    var suffix;
    for (var i = 0; i < suffices.length; i++) {
      suffix = suffices[i];
      if (n >= increment) {
        n /= increment;
      } else break;
    }

    n = Math.round(n * precision) / precision;

    return n + ' ' + suffix;
  }

  // Will be invoked directly from article HTML, where the templating engine will inject the fileSizeTable data.
  $.fn.populateFileSizes = function (fileSizeTable) {
    $('.file-size').each(function (index, element) {
      var $el = $(element);
      var doi = $el.attr('data-doi');
      var fileSize = $el.attr('data-size');
      var size = fileSizeTable[ doi][ fileSize];
      $el.text('(' + formatHumanReadableByteSize(size) + ')');
    });
  };

})(jQuery);
