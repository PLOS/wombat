var CitedBox = {
  data: {},
  sourceOrder: ['scopus','crossref','pubmed','wos', 'pmceurope', 'pmceuropedata', 'datacite', 'google']
};

(function ($) {

  CitedBox.element = $('#relatedCites');
  CitedBox.loadingEl = $('#relatedCitesSpinner');

  CitedBox.init = function (data) {
    var context = this;
    this.data = data;
    this.element.hide();

    var sourcesUnordered = _.filter(data.sources, function (source) { return _.contains(context.sourceOrder, source.name) && source.metrics.total > 0; });
    var sourceOrderKeys = _.invert(_.object(_.pairs(this.sourceOrder)));
    this.sources = _.sortBy(sourcesUnordered, function(source) { return sourceOrderKeys[source.name] });



    this.loadingEl.hide();
    this.element.show();
    MetricsTab.registerLoadedComponent(this);
  };

  CitedBox.newArticleError = function () {

  };

  CitedBox.dataError = function () {

  };

  CitedBox.createTile = function (source) {
    var tile = new MetricTile(source.display_name, null, WombatConfig.imgPath + "logo-" + source.name + '.png', source.metrics.total);
    var tileElement = null;

    if(_.has(source, 'events_url') && !_.isEmpty(source.events_url)) {
      tileElement = tile.createWithLink();
    }
    else {
      tileElement = tile.createWithNoLink();
    }

    $(this.element).append(tileElement);
  };

})(jQuery);