/**
 * Created by pgrinbaum on 7/17/14.
 */

$( document ).ready(function() {
 //carousel

  carousel.init();
 //tooltip - requires dotdotdot
  $(".truncated-tooltip").dotdotdot({
    height: 45
  });

  var journal_blogfeed = $('#blogs').attr('data-feed-url');
  var journal_blogpostcount = $('#blogs').attr('data-postcount');
  var postCountInt = parseInt(journal_blogpostcount);
  var journalBlogContainer = document.getElementById("blogrss");

  feedLoaded(journal_blogfeed,postCountInt,journalBlogContainer);
});


