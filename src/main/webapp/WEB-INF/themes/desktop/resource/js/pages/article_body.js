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
    var doi = $media.data('doi');
    $media.getArticleSummary(doi,
        function (data) {
          var mediaCoverageCount = getMediaCoverageCount(data);
          $media.find('#media-coverage-count')
              .text('(' + mediaCoverageCount + ')')
              .removeAttr('data-visibility');
        });
  }

  /// write article nav
  $article = $('#artText');

  $('#nav-article').buildNav({
    content: $article
  });

  $('#nav-article').floatingNav({
    sections: $article.children()
  });

  addMediaCoverageLink();

})(jQuery);
