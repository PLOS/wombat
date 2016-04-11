var MetricsSavedSection;

(function ($) {

  MetricsSavedSection = MetricsTabComponent.extend({
    $element: $('#relatedBookmarks'),
    $loadingEl: $('#relatedBookmarksSpinner'),
    sourceOrder: ['citeulike', 'connotea', 'mendeley'],
    loadData: function (data) {
      this._super(data);

      this.createTiles();

      this.afterLoadData();
    },
    dataError: function () {
      //For saved section in case of error we hide all the section, including title. Verify if we have a #savedCard to hide the card also on mobile.
      if($('#savedCard').length) {
        $('#savedCard').hide();
      }
      else {
        $('#socialNetworksOnArticleMetricsPage').css("display", "none");
      }
    },
    newArticleError: function () {
      this.dataError();
    }
  });

})(jQuery);