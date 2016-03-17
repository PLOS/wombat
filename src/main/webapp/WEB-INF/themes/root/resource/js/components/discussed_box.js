var DiscussedBox;

(function ($) {

  DiscussedBox = MetricsTabComponent.extend({
    element: $('#relatedBlogPosts'),
    loadingEl: $('#relatedBlogPostsSpinner'),
    sourceOrder: ['researchblogging', 'scienceseeker', 'nature', 'wordpress', 'wikipedia', 'twitter', 'facebook', 'reddit'],
    loadData: function (data) {
      this._super(data);
      var that = this;

      _.each(this.sources, function (source) {
        switch (source.name) {
          case 'facebook':
            that.createFacebookTile(source);
            break;
          case 'twitter':
            that.createTwitterTile(source);
            break;
          default:
            var tile = new MetricTile(source);
            tile.createTile(that.element);
            break;
        }
      });

      $('#notesAndCommentsOnArticleMetricsTab').appendTo(this.element);
      $('#trackbackOnArticleMetricsTab').appendTo(this.element);

      this.afterInit();
    },
    dataError: function () {
      this.loadingEl.hide();
      this.element.show();
    },
    newArticleError: function () {
      this.dataError();
    },
    createFacebookTile: function (source) {
      var tile = new MetricTile(source);
      tile.createTile(this.element);
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
    },
    createTwitterTile: function (source) {
      source.events_url = ALM_CONFIG.hostname + '/works/doi.org/' + ArticleData.doi + "?source_id=twitter";
      var tile = new MetricTile(source);
      tile.createTile(this.element);
    }
  });

})(jQuery);