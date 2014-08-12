/**
 * Created by pgrinbaum on 7/17/

 * requires:  hoverIntent
 */

//  Menu Function
(function ($) {

  $.fn.menuDrop = function (action, options) {
    options = $.extend({}, defaults, options);

    var defaults = {
      child: '.dropdown'
    };

    if (action === "show") {
      this.addClass("hover");
      $(options.child, this).css('visibility', 'visible');
      return this;
    }

    if (action === "hide") {
      this.removeClass("hover");
      $(options.child, this).css('visibility', 'hidden');
      return this;
    }

  };

}(jQuery));

hover_delay = {

  init: function () {
    // kick things off
    var $dropdown = $('li.has-dropdown');
    // if mobile, use modernizer to check for touch events. If so then:
//      1. add needsclick class so fastclick won't do it's magic
//      2. use hover instead of hoverIntent - hoverIntent adds timing to the hover we don't want for touch devices. '
    // 3. we need to invoke the hide action AFTER the click because safari on mobile will persist hover state when you go to another page.

    if ($('html.touch').length) {
      $dropdown.
          addClass('needsclick').
          hover(
            function () {$(this).menuDrop("show");},
            function () {$(this).menuDrop("hide");}
          );
      // invoke the hide menu
      $('.dropdown a').on({ 'click':
          function () {
            $.Deferred().done($dropdown.menuDrop("hide"));}
      });
    } else {
      //HoverIntent.js is used for the main navigation delay on hover
      $dropdown.hoverIntent(
          function () {$(this).menuDrop("show");},
          function () {$(this).menuDrop("hide");}
      );
    }
  }
}