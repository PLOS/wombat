var SavedBox = {
  data: {},
  sourceOrder: ['citeulike', 'connotea', 'mendeley']
};

(function ($) {

  SavedBox.element = $('#relatedBookmarks');
  SavedBox.loadingEl = $('#relatedBookmarksSpinner');

  SavedBox.formatName = function (name) {
    name = name.replace(/\s/g, "");
    return name;
  };

  SavedBox.init = function (data) {
    var context = this;
    this.data = data;
    this.element.hide();

    var sourcesUnordered = _.filter(this.data.sources, function (source) { return _.contains(context.sourceOrder, source.name) && source.metrics.total > 0; });
    var sourceOrderKeys = _.invert(_.object(_.pairs(this.sourceOrder)));
    this.sources = _.sortBy(sourcesUnordered, function(source) { return sourceOrderKeys[source.name] });

    if(this.sources.length) {
      _.each(this.sources, function (source) {
        source.display_name = context.formatName(source.display_name);
        switch (source.name) {
          case 'mendeley':
            context.createMendeleyTile(source);
            break;
          default:
            MetricTile.createTile(source, context.element);
            break;
        }
      });
    }
    else {
      this.dataError();
    }

    this.loadingEl.hide();
    this.element.show();
    MetricsTab.registerLoadedComponent(this);
  };

  SavedBox.createMendeleyTile = function (source) {
    MetricTile.createTile(source, this.element);
    var tooltipTemplate = _.template($('#metricsTileMendeleyTooltipTemplate').html());
    var tooltipData = {
      individuals: source.events.reader_count,
      groups: source.events.group_count
    };

    $('#MendeleyOnArticleMetricsTab')
      .attr("data-js-tooltip-hover", "trigger")
      .append(tooltipTemplate(tooltipData));
    tooltip_hover.init();
  };

  SavedBox.newArticleError = function () {

  };

  SavedBox.dataError = function () {
    $('#socialNetworksOnArticleMetricsPage').css("display", "none");
  };

})(jQuery);