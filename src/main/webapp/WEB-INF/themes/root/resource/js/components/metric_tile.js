var MetricTile;

(function ($) {
  MetricTile = Class.extend({
    init: function(source){
      this.name = source.display_name;
      this.imageSrc = WombatConfig.imgPath + "logo-" + source.name + '.png';
      this.linkText = source.metrics.total;
      this.hasUrl = false;
      if(_.has(source, 'events_url') && !_.isEmpty(source.events_url)) {
        this.hasUrl = true;
        this.url = source.events_url.replace(/"/g, "%22");
      }
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
      var tileElement = null;

      if(this.hasUrl) {
        tileElement = this.createWithLink();
      }
      else {
        tileElement = this.createWithNoLink();
      }

      $(elementToAppend).append(tileElement);
    }
  });
})(jQuery);