var MetricsCitedSection;

(function ($) {

  var doi = encodeURI(ArticleData.doi);
  var docURL = "http://dx.plos.org/" + doi.replace("info%3Adoi/", "");
  var googleCitationUrl = WombatConfig.metrics.googleScholarCitationUrl + docURL;

  MetricsCitedSection = MetricsTabComponent.extend({
    $element: $('#relatedCites'),
    $loadingEl: $('#relatedCitesSpinner'),
    $headerEl: $('#citedHeader'),
    sourceOrder: ['scopus','crossref','pubmed','wos', 'pmceurope', 'pmceuropedata', 'datacite'],
    loadData: function (data) {
      this._super(data);

      //Create manually the Google Scholar tile
      var googleTileSource = {
        display_name: "GoogleScholar",
        events_url: googleCitationUrl,
        name: 'google-scholar',
        metrics: {
          total: "Search"
        }
      };
      this.sources.push(googleTileSource);
      this.createTiles();

      this.afterLoadData();
    },
    dataError: function () {
      //In case of error we show the google scholar link
      var template = _.template($('#citedSectionNoDataTemplate').html());
      var templateHtml = template({googleLink: googleCitationUrl});

      this.$element.append(templateHtml).show();
      this.$loadingEl.hide();
    },
    newArticleError: function () {
      this.dataError();
    }
  });

})(jQuery);
