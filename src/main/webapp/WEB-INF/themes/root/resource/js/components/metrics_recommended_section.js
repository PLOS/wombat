var MetricsRecommendedSection;

(function ($) {

  MetricsRecommendedSection = MetricsTabComponent.extend({
    $element: $('#f1kContent'),
    $loadingEl: $('#f1KSpinner'),
    $headerEl: $('#f1kHeader'),
    sourceOrder: ['f1000'],
    loadData: function (data) {
      this._super(data);

      this.createTiles();

      this.afterLoadData();
    },
    dataError: function () {
      this.$element.hide();
      this.$loadingEl.hide();
      this.$headerEl.hide();
    },
    newArticleError: function () {
      this.dataError();
    }
  });

})(jQuery);