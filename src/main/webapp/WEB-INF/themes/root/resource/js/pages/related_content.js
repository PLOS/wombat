var ArticleRelatedContent;

(function ($) {

  ArticleRelatedContent = Class.extend({

    $mediaCoverageEl: $('#media-coverage-data'),
    mediaCoverageData: null,
    mediaCoverageSections: [
      {
        name: "News",
        title: "News Media Coverage",
        eventTypes: ['News']
      },
      {
        name: "Blog",
        title: "Blog Coverage",
        eventTypes: ['Blog']
      },
      {
        name: "Other",
        title: "Related Resources",
        eventTypes: []
      },
    ],
    modalFormEl: '#media-coverage-modal',
    modalErrorCloseTimeout: 3000,
    modalSuccessCloseTimeout: this.modalErrorCloseTimeout/2,

    init: function () {


      var query = new AlmQuery();
      var that = this;

      query.getArticleDetail(ArticleData.doi)
        .then(function (articleData) {
          var data = articleData[0];
          var mediaCoverageSource = _.findWhere(data.sources, {name: 'articlecoveragecurated'});

          if(mediaCoverageSource && mediaCoverageSource.events.length) {
            that.mediaCoverageData = _.map(mediaCoverageSource.events, function (item) { return item.event; });
          }
          else {
            throw new ErrorFactory('NoRelatedContentError', '[ArticleRelatedContent::init] - The article has no related content.');
          }

          that.loadMediaCoverage();
        })
        .fail(function (error) {
          console.log(error);
        });

    },

    loadMediaCoverage: function () {
      var that = this;
      var usedTypes = [];
      var renderedSections = 0;
      var sectionTemplate = _.template($('#articleRelatedContentSectionTemplate').html());
      _.each(this.mediaCoverageSections, function (section) {
        usedTypes = usedTypes.concat(section.eventTypes);

        var items = _.filter(that.mediaCoverageData, function (item) {
          var typeValidation = (_.indexOf(section.eventTypes, item.type) >= 0);
          if(!section.eventTypes.length) {
            typeValidation = (_.indexOf(usedTypes, item.type) < 0);
          }
          return typeValidation &&
            (item.link_state = "APPROVED") &&
            !_.isEmpty(item.title) &&
            !_.isEmpty(item.publication);
        });

        if(items.length) {
          that.$mediaCoverageEl.append(sectionTemplate({ section: section, items: items }));
          renderedSections++;
        }
      });

      if(!renderedSections) {
        that.$mediaCoverageEl.append('<p><br>No media coverage found for this article.</p>');
      }
    },


  });

  new ArticleRelatedContent();

})(jQuery);