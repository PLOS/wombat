var ArticleData = {};

(function ($) {
  ArticleData = {
    doi:  $('meta[name=citation_doi]').attr('content'),
    date: $('meta[name=citation_date]').attr('content'),
    title: $('meta[name=citation_title]').attr('content')
  };

})(jQuery);