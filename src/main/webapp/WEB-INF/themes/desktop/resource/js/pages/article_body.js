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
  var hash = window.location.hash.substring(1);
  console.log("hash : " + hash);

    $('.article-body a[href^="#"]').click(function(event) {

      var $target = $(event.target),
          target_hash = $(event.target.hash),
          target_hash2 = $target.attr('href').substring(1),
          title_height = $('.topVisible').innerHeight();

      console.log("$target : " + $target);
      console.log("target_hash2 : " + target_hash2);


        var target_hash_data = $target.attr('href'),
            target_link = target_hash_data.replace('#', ''),
            a_tag = $("a[name='"+ target_link +"']"),
            a_tag_position = a_tag.offset().top;
      console.log("a_tag_position : " + a_tag_position);
        console.log("height : " + title_height);

      var targetOffset = (a_tag_position + title_height);
      console.log("targetOffset : " + targetOffset);
        event.preventDefault();
      $('body').animate({scrollTop: targetOffset}, 400, function() {
        location.hash = target_hash_data;
        alert ($(document).scrollTop().valueOf());

      });

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
