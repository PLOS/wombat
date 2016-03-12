var MetricTile = function (name, url, imageSrc, linkText) {
  this.name = name;
  this.url = url;
  this.imageSrc = imageSrc;
  this.linkText = linkText;
};

(function ($) {

  MetricTile.prototype.createWithLink = function () {
    var metricsTileTemplate = _.template($('#metricsTileTemplate').html());
    return  metricsTileTemplate({url: this.url, name: this.name, imgSrc: this.imageSrc, linkText: this.linkText});
  };

  MetricTile.prototype.createWithNoLink = function () {
    var metricsTileTemplate = _.template($('#metricsTileTemplateNoLink').html());
    return  metricsTileTemplate({name: this.name, imgSrc: this.imageSrc, linkText: this.linkText});
  };

})(jQuery);