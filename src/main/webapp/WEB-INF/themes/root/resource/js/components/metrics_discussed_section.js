var MetricsDiscussedSection;

(function ($) {

  MetricsDiscussedSection = MetricsTabComponent.extend({
    $element: $('#relatedBlogPosts'),
    $loadingEl: $('#relatedBlogPostsSpinner'),
    $headerEl: $('#discussedHeader'),
    sourceOrder: ['researchblogging', 'scienceseeker', 'nature', 'wordpress', 'wikipedia', 'twitter', 'facebook', 'reddit'],
    loadData: function (data) {
      this._super(data);
      this.createTiles();

      //Add disclaimer texts to the main element
      $('#notesAndCommentsOnArticleMetricsTab').appendTo(this.$element);
      $('#trackbackOnArticleMetricsTab').appendTo(this.$element);

      this.afterLoadData();
    },
    emptyComponent: function () {
      //This function calls show component because we never hide the discussed section, because of the comments tile
      this.showComponent();
    },
    dataError: function () {
      //For discussed section in case of error we show the elements because of the comment tile (it's compiled by the template)
      this.$loadingEl.hide();
      this.$element.show();
    },
    newArticleError: function () {
      this.dataError();
    }
  });

})(jQuery);