var Signposts;

(function ($) {

  Signposts = Class.extend({

    $element: $('#almSignposts'),

    isDataValid: function (data) {
      return !_.isUndefined(data);
    },
    init: function() {
      var that = this;
      var query = new AlmQuery();

      query.getArticleDetail(ArticleData.doi)
        .then(function (articleData) {
          var data = articleData.data[0];
          if(that.isDataValid(data)) {
            return data;
          }
          else if(query.isArticleNew()) {
            throw new ErrorFactory('NewArticleError', '[Signposts::init] - The article is too new to have data.');
          }
          else {
            throw new ErrorFactory('InvalidDataError', '[Signposts::init] - The article data is invalid');
          }
        })
        .then(function (data) {
          var template  = _.template($('#signpostsTemplate').html());
          var templateData = {
            saveCount: data.saved,
            citationCount: data.cited,
            shareCount: data.discussed,
            viewCount: data.viewed
          };

          that.$element.html(template(templateData));

          if(!_.isUndefined(data.sources)) {
            var scopus = _.findWhere(data.sources, { name: 'scopus' });
            if(scopus.metrics.total > 0) {
              $('#almCitations').find('.citations-tip a').html('Scopus data unavailable. Displaying Crossref citation count.');
            }
          }

          //Initialize tooltips
          tooltip_hover.init();
        })
        .fail(function (error) {
          switch(error.name) {
            case 'NewArticleError':
              var template  = _.template($('#signpostsNewArticleErrorTemplate').html());
              break;
            default:
              var template  = _.template($('#signpostsGeneralErrorTemplate').html());
              break;
          }

          that.$element.html(template());
        });

    }
  });

  new Signposts();

})(jQuery);