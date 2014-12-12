/**
 * Created by pgrinbaum on 12/11/14.
 */
(function ($) {

 //   function handleSocialClick(event) {
 //     alert($(e.target).attr('title'));
//      ga('send', 'event', 'Article', 'Share', $(event.target).attr('title') 4);
//    };

  var sharelink = '.share-article ul li a',
      printlink = '#printArticle #printBrowser';
      //      print = '.if(typeof(_gaq) != 'undefined'){ _gaq.push(['_trackEvent','Article', 'Print', 'Click']); } ';

  var ga_track = function ( ga_category, ga_action, ga_label) {
   alert('track');
    ga('send', 'event', ga_category, ga_action, ga_label);
  };

// shareLink
  $(sharelink).on('click', function(event) {    //TODO: abstract this out
        var title = $(event.target).attr('title');
        ga('send', 'event', 'Article', 'Share', title);
      }
  );

$(printlink).on('click', function(event) {    //TODO: abstract this out
  ga('send', 'event','Article', 'Print', 'Click');
});



})(jQuery);
