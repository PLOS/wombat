var MetricTile;

(function ($) {
  MetricTile = Class.extend({
    init: function(source){
      this.source = source;
    },
    createWithLink: function () {
      var metricsTileTemplate = _.template($('#metricsTileTemplate').html());
      return  metricsTileTemplate({url: this.url, name: this.name, imgSrc: this.imageSrc, linkText: this.linkText});
    },
    createWithNoLink: function () {
      var metricsTileTemplate = _.template($('#metricsTileTemplateNoLink').html());
      return  metricsTileTemplate({name: this.name, imgSrc: this.imageSrc, linkText: this.linkText});
    },
    createTile: function (elementToAppend) {
      this.beforeCreateTile();
      var tileElement = null;

      if(this.hasUrl) {
        tileElement = this.createWithLink();
      }
      else {
        tileElement = this.createWithNoLink();
      }

      $(elementToAppend).append(tileElement);
      this.afterCreateTile();
    },
    beforeCreateTile: function () {
      switch(this.source.name) {
        case 'twitter':
          this.source.events_url = ALM_CONFIG.hostname + '/works/doi.org/' + ArticleData.doi + "?source_id=twitter";
          break;
        default:
          break;
      }

      this.name = this.source.display_name;
      this.imageSrc = WombatConfig.imgPath + "logo-" + this.source.name + '.png';
      this.linkText = this.source.metrics.total;
      this.hasUrl = false;
      if(_.has(this.source, 'events_url') && !_.isEmpty(this.source.events_url)) {
        this.hasUrl = true;
        this.url = this.source.events_url.replace(/"/g, "%22");
      }
    },
    afterCreateTile: function () {
      var tooltipTemplate = false;
      var tooltipData = false;
      var tooltipElementId = false;
      switch (this.source.name) {
        case 'facebook':
          tooltipTemplate = _.template($('#metricsTileFacebookTooltipTemplate').html());
          tooltipData = {
            likes: this.source.events[0].like_count,
            shares: this.source.events[0].share_count,
            comments: this.source.events[0].comment_count
          };
          tooltipElementId = '#FacebookOnArticleMetricsTab';
          break;
        default:
          break;
      }

      if(tooltipTemplate && tooltipData) {
        $(tooltipElementId)
          .attr("data-js-tooltip-hover", "trigger")
          .append(tooltipTemplate(tooltipData));
        tooltip_hover.init();
      }
    }
  });
})(jQuery);