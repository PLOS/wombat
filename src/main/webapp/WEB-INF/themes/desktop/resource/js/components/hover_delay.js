/**
 * Created by pgrinbaum on 7/17/

 * requires:  hoverIntent
 * requires: menu_drop
 */

(function ($) {
  hover_delay = {

    init: function () {
      // kick things off
      var $menu_drop_selector = $('li.has-dropdown');
      // if mobile, use modernizer to check for touch events. If so then:
//      1. add needsclick class so fastclick won't do it's magic
//      2. use hover instead of hoverIntent - hoverIntent adds timing to the hover we don't want for touch devices. '
      // 3. we need to invoke the hide action AFTER the click because safari on mobile will persist hover state when you go to another page.

      if ($('html.touch').length) {
        $menu_drop_selector.addClass('needsclick').on('touchstart',
          function () {
            $(this).menu_drop('show').siblings().menu_drop('hide');
          }
        ).bind('touchstart', function(event){
              event.stopPropagation(); //Make all touch events stop at the .filters ul.children container element
            });

        $(document).bind('touchstart', function(e) {
          $menu_drop_selector.menu_drop('hide'); //Close filters drop-downs if user taps ANYWHERE in the page
        });

        // invoke the hide menu
        $('.dropdown a').on({ 'click': function () {
          $.Deferred().done($menu_drop_selector.menu_drop('hide'));
        }
        });
      } else {
        //HoverIntent.js is used for the main navigation delay on hover
        $menu_drop_selector.hoverIntent(
          function () {$(this).menu_drop('show');},
          function () {$(this).menu_drop('hide');}
        );
      }
    }
  }
})(jQuery);