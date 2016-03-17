var MetricsTabComponent;

(function ($) {
  MetricsTabComponent = Class.extend({
    loadingEl: null,
    element: null,
    sourceOrder: [],
    data: [],
    sources:[],
    init: function () {
      this.loadingEl.show();
      this.element.hide();
    },
    loadData: function (data) {
      console.log(data);
      this.data = data;
      this.filterSources();
    },
    afterInit: function () {
      this.loadingEl.hide();
      this.element.show();
    },
    filterSources: function () {
      var that = this;
      //Filter only the sources that are in the sourceOrder and metrics total is bigger then 0
      var sourcesUnordered = _.filter(this.data.sources, function (source) {
        return _.contains(that.sourceOrder, source.name) && source.metrics.total > 0;
      });
      //Transform the sourceOrder array in object and invert the value with the key
      var sourceOrderKeys = _.invert(_.object(_.pairs(this.sourceOrder)));
      //Reorder sources based on the key of that value in the original sourceOrder
      this.sources = _.sortBy(sourcesUnordered, function(source) { return sourceOrderKeys[source.name] });
    },
    newArticleError: function () {
      this.element.hide();
      this.loadingEl.hide();
    },
    dataError: function () {
      this.element.hide();
      this.loadingEl.hide();
    }
  });
})(jQuery);