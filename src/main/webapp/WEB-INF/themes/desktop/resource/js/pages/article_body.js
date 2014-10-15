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
//  var hash = window.location.hash.substring(1);
//  console.log("hash : " + hash);

    $('.article-body').click(function(event) {
      var $target = $(event.target),
          target_hash = $(event.target.hash),
          title_height = $('.topVisible').innerHeight();
      console.log("target_hash : " + target_hash);
      if (target_hash) {
        var target_hash_data = $target.attr('href'),
            target_link = target_hash_data.replace('#', ''),
            a_tag = $("a[name='"+ target_link +"']");

      var targetOffset = a_tag.offset().top - (title_height + 20 );
        event.preventDefault();
      $('body').animate({scrollTop: targetOffset}, 400, function() {
        location.hash = target_hash_data;

      });
      };
    });



  /// write article nav
  $article = $('.article-content');

  $('#nav-article').buildNav({
    content: $article
  });

  $('#nav-article').floatingNav({
    sections: $article.find('div.toc-section')
  });

  addMediaCoverageLink();

})(jQuery);
