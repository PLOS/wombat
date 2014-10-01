(function ($) {


  /// write article nav
  $article = $('#artText');

  $('#nav-article').buildNav({
    content: $article
  });

  $('#nav-article').floatingNav({
    sections: $article.find('div.section')
  });

})(jQuery);
