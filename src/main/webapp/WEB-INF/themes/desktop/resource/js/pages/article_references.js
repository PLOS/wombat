(function ($) {
  referenceValidation = Class.extend({

    init: function () {

      this.getJSON();

    },


    getJSON: function () {
      that = this;

      $('.references li').one('click', 'ul.reflinks li:first-child a', function (event) {

        var queryStringAuthor = $(this).attr('data-author');
        var queryStringTitle = $(this).attr('data-title');
        var queryStringCit = $(this).attr('data-cit');
        var doiAvailableText = 'doi-provided';

        if (queryStringCit !== doiAvailableText) {

          var $that = $(this);

          var queryStringConcat = 'query.author=' + queryStringAuthor + '&query.title=' + queryStringTitle;
          var crossrefApi = "http://api.crossref.org/works?query=" + queryStringConcat + "&sort=score&rows=1";
          var DOIResolver = 'http://dx.doi.org/';
          var crossrefSearchString = 'http://search.crossref.org/?q=' + queryStringCit;
          var articleLink = null;


          event.preventDefault();

          $.ajax({
                url: crossrefApi,
                beforeSend: (
                    function () {
                      $that.addClass('link-disabled');
                    })
              })
              .success(
                  function (data) {
                    var DOIs = data.message.items[0].DOI;
                    var titleAPI = data.message.items[0].title;
                    var titleXML = queryStringTitle.replace(/%20/g, ' ');
                    var titleXMLConcat = s.trim(titleXML, '.');

                    $that.attr('href', DOIResolver + DOIs);

                    if (titleAPI === titleXMLConcat) {
                      articleLink = DOIResolver + DOIs;
                    } else {
                      articleLink = crossrefSearchString;
                    }
                    ;
                  }
              )
              .error(
                  function () {
                    articleLink = crossrefSearchString;
                  }
              )
              .done(function () {
                window.open(articleLink, '_new');
                $that.removeClass('link-disabled');
              });

        }
        ;
      });

    },


  });
  this.articleReferences = new referenceValidation();

})(jQuery);








