var MetricsTab = {};

(function ($) {

  MetricsTab.components = [(new MetricsViewedSection()), (new MetricsCitedSection()), (new MetricsSavedSection()), (new MetricsDiscussedSection()), (new MetricsRecommendedSection())];

  MetricsTab.getComponents = function () {
    return this.components;
  };

  MetricsTab.init = function () {
    var query = new AlmQuery();
    var that = this;

    query
      .getArticleDetail(ArticleData.doi)
      .then(function (articleData) {
        var data = articleData[0];
        _.each(that.getComponents(), function (value) { value.loadData(data); });
      })
      .fail(function (error) {
        switch(error.name) {
          case 'NewArticleError':
            _.each(that.getComponents(), function (value) { value.newArticleError(); });
            break;
          default:
            _.each(that.getComponents(), function (value) { value.dataError(); });
            break;
        }
      })
  };

  MetricsTab.init();

})(jQuery);