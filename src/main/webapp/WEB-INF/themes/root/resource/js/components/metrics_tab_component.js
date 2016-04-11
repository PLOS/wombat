/*
* MetricsTabComponent:
* This is the base class for any component in MetricsTab, it already has the functions needed to filter the sources and create the tiles.
* The MetricsTabComponent also encapsulate the essential methods to a MetricsTab component work (loadData, newArticleError and dataError).
* The 'loadData()' is responsible to load the data to the component, the 'newArticleError()' is responsible to handle 'NewArticleError' error and the 'dataError()' handles any other error.
*
* Every MetricsTab component should extend this class and be instantiated in 'MetricsTab.components'.
*
* Usage example:
*
* var MyComponent = MetricsTabComponent.extend({
*   $loadingEl: $('#MyComponentSpinner'),
*   $element: $('#MyComponent'),
*   sourceOrder: ['facebook', 'twitter', 'pmc'],
*
*   loadData: function (data) {
*     this._super(data);
*
*     this.createTiles();
*
*     this.afterLoadData();
*   }
* });
*/

var MetricsTabComponent;

(function ($) {
  MetricsTabComponent = Class.extend({
    //Loading spinner jQuery Element
    $loadingEl: null,
    //The main jQuery element of the component, where the tiles should be appended.
    $element: null,
    //The order that the sources needs to be displayed in the tiles
    sourceOrder: [],
    //Filled by the loadData function
    data: [],
    //Filled by the filterSources function
    sources:[],

    init: function () {
      this.$loadingEl.show();
      this.$element.hide();
    },

    loadData: function (data) {
      //Fix for CrossRef, the metrics.total is not calculated by ALM
      var crossrefIndex = _.findIndex(data.sources, {name: 'crossref'});
      if(crossrefIndex >= 0){
        var crossref = data.sources[crossrefIndex];
        if(!_.has(crossref, 'metrics')) {
          crossref.metrics = { total: 0 };
        }

        if(_.has(crossref, 'by_month') && crossref.metrics.total <= 0) {
          var total = _.reduce(crossref.by_month, function (memo, obj) { return memo + obj.total }, 0);
          crossref.metrics.total = total;
        }
      }

      this.data = data;
      this.filterSources();
    },

    afterLoadData: function () {
      this.$loadingEl.hide();
      this.$element.show();
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

    formatName: function (name) {
      //Remove spaces
      name = name.replace(/\s/g, "");
      // removing registered trademark symbol from web of science
      name = name.replace("\u00ae", "");
      return name;
    },

    createTiles: function () {
      var that = this;
      _.each(this.sources, function (source) {
        source.display_name = that.formatName(source.display_name);
        var tile = new MetricTile(source);
        tile.createTile(that.$element);
      });
    },

    newArticleError: function () {
      this.$element.hide();
      this.$loadingEl.hide();
    },

    dataError: function () {
      this.$element.hide();
      this.$loadingEl.hide();
    }

  });
})(jQuery);