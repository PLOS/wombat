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

})(jQuery);
