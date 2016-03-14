var MetricTile = {
  name: null,
  url: null,
  imageSrc: null,
  linkText: null
};

(function ($) {

  MetricTile.createWithLink = function () {
    var metricsTileTemplate = _.template($('#metricsTileTemplate').html());
    return  metricsTileTemplate({url: this.url, name: this.name, imgSrc: this.imageSrc, linkText: this.linkText});
  };

  MetricTile.createWithNoLink = function () {
    var metricsTileTemplate = _.template($('#metricsTileTemplateNoLink').html());
    return  metricsTileTemplate({name: this.name, imgSrc: this.imageSrc, linkText: this.linkText});
  };

  MetricTile.createTile = function (source, elementToAppend) {
    this.name = source.display_name;
    this.imageSrc =  WombatConfig.imgPath + "logo-" + source.name + '.png';
    this.linkText = source.metrics.total;
    var tileElement = null;

    if(_.has(source, 'events_url') && !_.isEmpty(source.events_url)) {
      this.url = source.events_url.replace(/"/g, "%22");
      tileElement = this.createWithLink();
    }
    else {
      tileElement = this.createWithNoLink();
    }

    $(elementToAppend).append(tileElement);
  }

})(jQuery);