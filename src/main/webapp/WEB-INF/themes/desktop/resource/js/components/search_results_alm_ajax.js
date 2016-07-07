
var SearchResultsALMs;

(function ($) {

  SearchResultsALMData = Class.extend({

    //TODO: change this to an object
    containerID: '.search-results-alm-container',
    DOIlist: [],


  init: function (DOIlist) {
     this.setDOIList(DOIlist);
      this.getALMData();
    },

    setDOIList: function(DOIlist){

      var that = this;

      $(this.containerID).each(function (DOIlist) {
        var $this = $(this);
        that.DOIlist.push([$this.data('doi')]);
      });

    },
    getDOIList: function () {

      return this.DOIlist;
    },
    getALMData: function(){
      var that = this;

      var query = new AlmQuery();
      var validator = new AlmQueryValidator({checkSources: false});

      query
          .setDataValidator(validator)
          .getArticleSummary(that.DOIlist)
          .then(function (articleData) {

            var data = articleData;
            that.showALMData(data)
          })
          .fail(function (error) {
              that.showALMErrorMessage();
          });
    },
    showALMData: function(data){
      var that = this;

      _.each(data, function (i) {
        var doi = i.doi;
        var templateData = {
          saveCount: i.saved,
          citationCount: i.cited,
          shareCount: i.discussed,
          viewCount: i.viewed
        };

        var template = _.template($('#searchResultsAlm').html());
        $(that.containerID).filter("[data-doi='" + doi + "']").html(template(templateData));

      });
    },
    showALMErrorMessage: function (error) {
      var that = this;

      var template = _.template($('#searchResultsAlmError').html());
      $(this.containerID).html(template());
      console.log(error);
    }
  });


})(jQuery);