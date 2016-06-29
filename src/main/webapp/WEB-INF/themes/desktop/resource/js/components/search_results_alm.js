//  function handleUndefinedOrZeroMetric(metric) {
//    return metric == undefined || metric == 0 ? 'None' : metric;
//  }
//
//  function appendOrRemoveLink(link, metric) {
//    metric = handleUndefinedOrZeroMetric(metric);
//    if (metric == 'None') {
//      link.html(link.html() + ' ' + metric);
//      link.contents().unwrap();
//    } else {
//      link.append(metric);
//    }
//  }
//
//function doALMS() {
//  $('.search-results-alm').each(function () {
//    var $alm = $(this);
//    var doi = $alm.data('doi');
//    $alm.getArticleSummary(doi, function (almData) { //success function
//
//      var almLinks = $alm.find('a');
//      var viewsLink = $(almLinks[0]);
//      appendOrRemoveLink(viewsLink, almData.viewed);
//
//      var citationsLink = $(almLinks[1]);
//      appendOrRemoveLink(citationsLink, almData.cited);
//
//      var savesLink = $(almLinks[2]);
//      appendOrRemoveLink(savesLink, almData.saved);
//
//      var sharesLink = $(almLinks[3]);
//      appendOrRemoveLink(sharesLink, almData.shared);
//
//      $alm.siblings('.search-results-alm-loading').fadeOut('slow', function () {
//        $alm.fadeIn();
//      })
//    }, function () { //error function
//      $alm.siblings('.search-results-alm-loading').fadeOut('slow', function () {
//        $alm.siblings('.search-results-alm-error').fadeIn();
//      })
//    });
//  });
//}


var SearchResultsALMs;

(function ($) {

  SearchResultsALMs = Class.extend({

    $element: $('.search-results-alm-container'),

    init: function() {
      var that = this;
      var query = new AlmQuery();
      var validator = new AlmQueryValidator({ checkSources: false });
      var DOIlist = [];
      $('.search-results-alm-container').each(function(){var $this = $(this);
        DOIlist.push([ $this.data('doi') ]);});


      query
          .setDataValidator(validator)
          .getArticleSummary(DOIlist)
          .then(function (articleData) {
          console.log( $('.search-results-alm-container').attr('data-doi'));
            var data = articleData;
            _.each(data,function(i) {
              console.log(i);

            var doi = i.doi;
              var templateData = {
                saveCount: i.saved,
                citationCount: i.cited,
                shareCount: i.discussed,
                viewCount: i.viewed
              };
              console.log( templateData);

              //return templateDataALM;
              var template = _.template($('#search-results-alm').html());
              //var templateData = {
              //  //saveCount: data.saved,
              //  //citationCount: data.cited,
              //  //shareCount: data.discussed,
              //  views: data.viewed
              //};
              $(".search-results-alm-container[data-doi='"+doi+"']").html(template(templateData));

              //if(!_.isUndefined(data.sources)) {
              //  var scopus = _.findWhere(data.sources, { name: 'scopus' });
              //  if(scopus.metrics.total > 0) {
              //    $('#almCitations').find('.citations-tip a').html('Scopus data unavailable. Displaying Crossref citation count.');
              //  }
              //}
console.log('next')

            });
          })
          .fail(function (error) {
            
            console.log("error =" + error);
            console.log("hello");

            that.$element.fadeOut();

            var template  = _.template($('#search-results-alm').html());
            var templateDataALM = {
              //saveCount: data.saved
              //citationCount: data.cited,
              //shareCount: data.discussed,
              views: 'error'
            };

            //return templateDataALM;

            //switch(error.name) {
            //  case 'NewArticleError':
            //    //var template  = _.template($('#signpostsNewArticleErrorTemplate').html());
            //    break;
            //  default:
            //    //var template  = _.template($('#signpostsGeneralErrorTemplate').html());
            //    break;
            //}
            //
            $('.search-results-alm-container').html(template(templateDataALM));
          });

    }
  });


})(jQuery);