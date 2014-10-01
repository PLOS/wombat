$(document).ready(function () {

  /// write article nav
 var $article = $('#artText');

  $('#nav-article').buildNav({
    content: $article
  });

  $('#nav-article').floatingNav({
    sections:  $article.find('div.section')
  });

});
