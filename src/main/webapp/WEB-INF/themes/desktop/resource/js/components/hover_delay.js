/**
 * Created by pgrinbaum on 7/17/

 * requires:  hoverIntent
 */


hover_delay = {

  init: function () {
    // kick things off

    //HoverIntent.js is used for the main navigation delay on hover
    function showIt() {
      $(this).addClass("hover");
      $('.dropdown', this).css('visibility', 'visible');
    }

    function hideIt() {
      $(this).removeClass("hover");
      $('.dropdown', this).css('visibility', 'hidden');
    }

    // if mobile, use modernizer to check for touch events. If so then:
//      1. add needsclick class so fastclick won't do it's magic
//      2. use hover instead of hoverIntent - hoverIntent adds timign to the hover we dont' want for touch devices. '
    if ($('html.touch').length) {
      $('li.has-dropdown').addClass('needsclick').hover(showIt, hideIt);
    } else {
      $('li.has-dropdown').hoverIntent(showIt, hideIt);
    }
  }
}