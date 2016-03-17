var MetricsTab = {};

(function ($) {

  MetricsTab.components = [(new DiscussedBox())];

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
    var query = AlmQuery.init();
    var that = this;

    query.getArticleDetail(ArticleData.doi)
      .then(function (articleData) {
        var data = articleData.data[0];
        if(that.isDataValid(data)) {
          return data;
        }
        else if(that.isArticleNew()) {
          var err = new Error('[MetricsTab::init] - The article is too new to have data.');
          err.name = 'NewArticleError';
          throw err;
        }
        else {
          var err = new Error('[MetricsTab::init] - The article data is invalid');
          err.name = 'InvalidDataError';
          throw err;
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