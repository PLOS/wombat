var MetricsTab = {};

(function ($) {

  MetricsTab.components = [DiscussedBox];
  MetricsTab.loadedComponents = [];


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

  MetricsTab.onLoadedComponentRegistered = function (component) {

  };

  MetricsTab.registerLoadedComponent = function (component) {
    if(_.indexOf(this.getComponents(), component) !== -1) {
      this.loadedComponents.push(component);
      this.onLoadedComponentRegistered(component);
    }
  };

  MetricsTab.init = function () {
    var query = AlmQuery.init();
    var context = this;

    query.getArticleDetail(ArticleData.doi)
      .then(function (articleData) {
        var data = articleData.data[0];
        if(context.isDataValid(articleData.data[0])) {
          return data;
        }
        else if(context.isArticleNew()) {
          var e = new Error('[MetricsTab::init] - The article is too new to have data.');
          e.name = 'NewArticleError';
          throw e;
        }
        else {
          var e = new Error('[MetricsTab::init] - The article data is invalid');
          e.name = 'InvalidDataError';
          throw e;
        }
      })
      .then(function (data) {
        _.each(context.getComponents(), function (value) { value.init(data); });
      })
      .fail(function (error) {
        switch(error.name) {
          case 'NewArticleError':
            _.each(context.getComponents(), function (value) { value.newArticleError(); });
            break;
          default:
            _.each(context.getComponents(), function (value) { value.dataError(); });
            break;
        }
      })
  };

  MetricsTab.init();

})(jQuery);