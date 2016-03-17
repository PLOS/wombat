var DiscussedSection;

(function ($) {

  DiscussedSection = MetricsTabComponent.extend({
    $element: $('#relatedBlogPosts'),
    $loadingEl: $('#relatedBlogPostsSpinner'),
    sourceOrder: ['researchblogging', 'scienceseeker', 'nature', 'wordpress', 'wikipedia', 'twitter', 'facebook', 'reddit'],
    loadData: function (data) {
      this._super(data);
      this.createTiles();

      $('#notesAndCommentsOnArticleMetricsTab').appendTo(this.$element);
      $('#trackbackOnArticleMetricsTab').appendTo(this.$element);

      this.afterLoadData();
    },
    dataError: function () {
      this.$loadingEl.hide();
      this.$element.show();
    },
    newArticleError: function () {
      this.dataError();
    }
  });

})(jQuery);