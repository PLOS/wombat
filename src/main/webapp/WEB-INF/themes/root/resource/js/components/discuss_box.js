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

    var sourcesUnordered = _.filter(data.sources, function (source) { return _.contains(context.sourceOrder, source.name) && source.metrics.total > 0; });
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
          context.createTile(source);
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
    this.createTile(source);
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
    this.createTile(source);
  };

  DiscussedBox.createTile = function (source) {

    var tile = new MetricTile(source.display_name, null, WombatConfig.imgPath + "logo-" + source.name + '.png', source.metrics.total);
    var tileElement = null;

    if(_.has(source, 'events_url') && !_.isEmpty(source.events_url)) {
      tile.url = source.events_url.replace(/"/g, "%22");
      tileElement = tile.createWithLink();
    }
    else {
      tileElement = tile.createWithNoLink();
    }

    $(this.element).append(tileElement);
  }

})(jQuery);