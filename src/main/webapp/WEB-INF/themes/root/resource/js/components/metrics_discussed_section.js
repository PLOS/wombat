/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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