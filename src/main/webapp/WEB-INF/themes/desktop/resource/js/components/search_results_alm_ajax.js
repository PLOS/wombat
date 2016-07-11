var SearchResultsALMData;

(function ($) {

  SearchResultsALMData = Class.extend({

    containerID: '.search-results-alm-container',
    DOIlist: [],


    init: function (DOIlist) {
      this.setDOIList(DOIlist);
    },

    setDOIList: function (DOIlist) {
      this.DOIlist = DOIlist;
    },
    getDOIList: function () {
      return this.DOIlist;
    },
    processALMDataRequest: function () {
      var that = this;

      var query = new AlmQuery();
      var validator = new AlmQueryValidator({checkSources: false});

      query
          .setDataValidator(validator)
          .getArticleSummary(that.DOIlist)
          .then(function (data) {
            that.showALMData(data)
          })
          .fail(function (error) {
            that.showALMErrorMessage();
          });
    },
    showALMData: function (data) {
      var that = this;
      var template = _.template($('#searchResultsAlm').html());
      _.each(data, function (i) {
        var doi = i.doi;
        var templateData = {
          saveCount: i.saved,
          citationCount: i.cited,
          shareCount: i.discussed,
          viewCount: i.viewed
        };

        $(that.containerID).filter("[data-doi='" + doi + "']").html(template(templateData));

      });
    },
    showALMErrorMessage: function (error) {
      var template = _.template($('#searchResultsAlmError').html());
      $(this.containerID).html(template());
    }
  });


})(jQuery);