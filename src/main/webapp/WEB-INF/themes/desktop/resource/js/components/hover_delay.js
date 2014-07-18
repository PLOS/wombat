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

      $('li.has-dropdown').hoverIntent(showIt, hideIt);
    }
  };