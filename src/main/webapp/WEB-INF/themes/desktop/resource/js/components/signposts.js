var Signposts;

(function ($) {

  Signposts = Class.extend({

    $element: $('#almSignposts'),

    isDataValid: function (data) {
      return !_.isUndefined(data);
    },
    bindTooltips: function () {
      $('.metric-term').mouseenter(function () {

        clearTimeout($(this).data('mouseId'));
        $(this).addClass('show-tip');
        var tippy = $(this).next();

        $(tippy).fadeIn('fast').addClass('tippy');

      }).mouseleave(function () {
        var boxtop = $(this);
        var tippy = $(this).next();

        $(tippy).mouseenter(function () {

          var boxtop = $(tippy).prev();
          clearTimeout($(boxtop).data('mouseId'));
          if ($(boxtop).hasClass('show-tip')) {} else {$(boxtop).addClass('show-tip');}

        }).mouseleave(function () {
          var boxtop = $(tippy).prev();

          $(boxtop).removeClass('show-tip');

          $(tippy).fadeOut('fast');
        });

        var mouseId = setTimeout(function () {

          $(tippy).fadeOut('fast');

          $(boxtop).removeClass('show-tip');
        }, 250);

        $(boxtop).data('mouseId', mouseId);
      });
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
          that.bindTooltips();
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