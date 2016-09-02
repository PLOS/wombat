var Signposts;

(function ($) {

  Signposts = Class.extend({

    $element: $('#almSignposts'),

    init: function() {
      var that = this;
      var query = new AlmQuery();
      var validator = new AlmQueryValidator({ checkSources: false });

      query
        .setDataValidator(validator)
        .getArticleDetail(ArticleData.doi)
        .then(function (articleData) {
          var data = articleData[0];
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
              $('#almCitations').find('.citations-tip a').html('Displaying Scopus citation count.');
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