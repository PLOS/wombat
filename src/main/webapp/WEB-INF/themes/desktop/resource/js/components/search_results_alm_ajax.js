

var SearchResultsALMs;

(function ($) {

  SearchResultsALMs = Class.extend({


    init: function () {
      var that = this;
      var query = new AlmQuery();
      var validator = new AlmQueryValidator({checkSources: false});
      var DOIlist = [];
      $('.search-results-alm-container').each(function () {
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

              var template = _.template($('#search-results-alm').html());
              $(".search-results-alm-container[data-doi='" + doi + "']").html(template(templateData));

            });
          })
          .fail(function (error) {
            var template = _.template($('#search-results-alm-error').html());
            $('.search-results-alm-container').html(template());
          });

    }
  });


})(jQuery);