
var SearchResultsALMs;

(function ($) {
  var $ALMContainer = $('.search-results-alm-container');

  SearchResultsALMs = Class.extend({

  init: function () {
      var that = this;
      var query = new AlmQuery();
      var validator = new AlmQueryValidator({checkSources: false});
      var DOIlist = [];
    $ALMContainer.each(function () {
        var $this = $(this);
        DOIlist.push([$this.data('doi')]);
      });

      query
          .setDataValidator(validator)
          .getArticleSummary(DOIlist)
          .then(function (articleData) {

            var data = articleData;
            _.each(data, function (i) {

              var doi = i.doi;
              var templateData = {
                saveCount: i.saved,
                citationCount: i.cited,
                shareCount: i.discussed,
                viewCount: i.viewed
              };

              var template = _.template($('#searchResultsAlm').html());
              that.$ALMContainer.attr("data-doi", doi).html(template(templateData));

            });
          })
          .fail(function (error) {
              that.showErrorMessage();
          });

    },
    showErrorMessage: function () {
      var template = _.template($('#searchResultsAlmError').html());
      $ALMContainer.html(template());

    }
  });


})(jQuery);