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
  figshareWidget();

  var hasFigures = $('#artText .figure').length;

  if(hasFigures){

  $('#figure-carousel-section').buildFigureCarousel({});

  };

  function formatHumanReadableByteSize(bytes) {
    // Space before "Bytes". All others are concatenated to number with no space.
    var units = [' Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];

    var increment = 1000; // could change to 1024
    var roundingThreshold = units.indexOf('MB'); // for smaller units than this, show integers
    var precision = 100; // round to this precision if unit is >= roundingThreshold

    var n = bytes;
    var unitIndex = 0;
    for (; unitIndex < units.length; unitIndex++) {
      if (n >= increment) {
        n /= increment;
      } else break;
    }

    n = (unitIndex < roundingThreshold) ? Math.round(n) : Math.round(n * precision) / precision;

    return n + units[unitIndex];
  }

  // Will be invoked directly from article HTML, where the templating engine will inject the fileSizeTable data.
  $.fn.populateFileSizes = function (fileSizeTable) {

    if(hasFigures){
    $('.file-size').each(function (index, element) {
      var $el = $(element);
      var doi = $el.attr('data-doi');
      var fileSize = $el.attr('data-size');
      var size = fileSizeTable[ doi][ /**/fileSize];
      $el.text('(' + formatHumanReadableByteSize(size) + ')');
    });
    }
  };

  $('.table-download').on('click', function(){
    var table = $(this).parent(),
    figId = $(this).data('tableopen');
    return tableOpen(figId, "CSV", table);
  });

  $('.table-wrap .expand').on('click', function(){
    var table = $(this).parent(),
    figId = $(this).data('tableopen');
    return tableOpen(figId, "HTML", table);
  });

})(jQuery);
