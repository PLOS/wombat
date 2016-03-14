var DiscussedBox = {
  data: {},
  sourceOrder: ['researchblogging','scienceseeker', 'nature', 'wordpress', 'wikipedia', 'twitter', 'facebook', 'reddit']
};

(function ($) {

  DiscussedBox.element = $('#relatedBlogPosts');
  DiscussedBox.loadingEl = $('#relatedBlogPostsSpinner');

  DiscussedBox.init = function (data) {
    var context = this;
    this.data = data;
    this.element.hide();

    var sourcesUnordered = _.filter(this.data.sources, function (source) { return _.contains(context.sourceOrder, source.name) && source.metrics.total > 0; });
    var sourceOrderKeys = _.invert(_.object(_.pairs(this.sourceOrder)));
    this.sources = _.sortBy(sourcesUnordered, function(source) { return sourceOrderKeys[source.name] });

    console.log(this.sources);

    _.each(this.sources, function (source) {
      switch(source.name) {
        case 'facebook':
          context.createFacebookTile(source);
          break;
        case 'twitter':
          context.createTwitterTile(source);
          break;
        default:
          MetricTile.createTile(source, context.element);
          break;
      }
    });

    $('#notesAndCommentsOnArticleMetricsTab').appendTo(this.element);
    $('#trackbackOnArticleMetricsTab').appendTo(this.element);

    this.loadingEl.hide();
    this.element.show();
    MetricsTab.registerLoadedComponent(this);
  };

  DiscussedBox.newArticleError = function () {

  };

  DiscussedBox.dataError = function () {

  };

  DiscussedBox.createFacebookTile = function (source) {
    MetricTile.createTile(source, this.element);
    var tooltipTemplate = _.template($('#metricsTileFacebookTooltipTemplate').html());
    var tooltipData = {
      likes: source.events[0].like_count,
      shares: source.events[0].share_count,
      comments: source.events[0].comment_count
    };

    $('#FacebookOnArticleMetricsTab')
      .attr("data-js-tooltip-hover", "trigger")
      .append(tooltipTemplate(tooltipData));
    tooltip_hover.init();
  };

  DiscussedBox.createTwitterTile = function (source) {
    source.events_url = ALM_CONFIG.hostname + '/works/doi.org/' + ArticleData.doi + "?source_id=twitter";
    MetricTile.createTile(source, this.element);
  };

})(jQuery);