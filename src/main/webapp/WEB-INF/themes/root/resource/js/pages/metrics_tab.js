var MetricsTab = {};

(function ($) {

  MetricsTab.components = [(new MetricsDiscussedSection())];

  MetricsTab.isDataValid = function (data) {
    return (!_.isUndefined(data) && _.has(data, 'sources'));
  };

  MetricsTab.isArticleNew = function () {
    var publishDate = moment(ArticleData.date, "MMM DD, YYYY").toDate();
    var todayMinus48Hours = (new Date()).getTime() - 172800000;
    return (todayMinus48Hours < publishDate.getTime());
  };

  MetricsTab.getComponents = function () {
    return this.components;
  };

  MetricsTab.init = function () {
    var query = new AlmQuery();
    var that = this;

    query.getArticleDetail(ArticleData.doi)
      .then(function (articleData) {
        var data = articleData.data[0];
        if(that.isDataValid(data)) {
          return data;
        }
        else if(that.isArticleNew()) {
          throw new ErrorFactory('NewArticleError', '[MetricsTab::init] - The article is too new to have data.')
        }
        else {
          throw new ErrorFactory('InvalidDataError', '[MetricsTab::init] - The article data is invalid');
        }
      })
      .then(function (data) {
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