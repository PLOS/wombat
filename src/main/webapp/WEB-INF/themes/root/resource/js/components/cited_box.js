var CitedBox = {
  data: {},
  sourceOrder: ['scopus','crossref','pubmed','wos', 'pmceurope', 'pmceuropedata', 'datacite']
};

(function ($) {

  CitedBox.element = $('#relatedCites');
  CitedBox.loadingEl = $('#relatedCitesSpinner');

  var doi = encodeURI(ArticleData.doi);
  var docURL = "http://dx.plos.org/" + doi.replace("info%3Adoi/", "");
  var googleCitationUrl = WombatConfig.metrics.googleScholarCitationUrl + docURL;

  CitedBox.formatName = function (name) {
    name = name.replace(/\s/g, "");
    // removing registered trademark symbol from web of science
    name = name.replace("\u00ae", "");
    return name;
  };

  CitedBox.init = function (data) {
    var context = this;
    this.data = data;
    this.element.hide();

    var sourcesUnordered = _.filter(data.sources, function (source) { return _.contains(context.sourceOrder, source.name) && source.metrics.total > 0; });
    var sourceOrderKeys = _.invert(_.object(_.pairs(this.sourceOrder)));
    this.sources = _.sortBy(sourcesUnordered, function(source) { return sourceOrderKeys[source.name] });

    if(this.sources.length) {
      _.each(this.sources, function (source) {
          source.display_name = context.formatName(source.display_name);
          MetricTile.createTile(source, context.element);
      });
      var googleTileSource = {
        display_name: "GoogleScholar",
        events_url: googleCitationUrl,
        name: 'google-scholar',
        metrics: {
          total: "Search"
        }
      };
      MetricTile.createTile(googleTileSource, this.element)
    }
    else {
      this.dataError();
    }

    this.loadingEl.hide();
    this.element.show();
    MetricsTab.registerLoadedComponent(this);
  };

  CitedBox.newArticleError = function () {

  };

  CitedBox.dataError = function () {
    var template = _.template($('#citedBoxNoDataTemplate').html());
    var templateHtml = template({googleLink: googleCitationUrl});

    this.element.append(templateHtml).show();
    this.loadingEl.hide();
  };

})(jQuery);