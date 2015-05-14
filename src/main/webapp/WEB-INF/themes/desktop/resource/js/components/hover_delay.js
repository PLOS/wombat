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
      var selected_state = '.hover';
      // if mobile, use modernizer to check for touch events. If so then: use "touchstart" instead of hover  and make sure the menu is closeable

      if ($('html.touch').length) {
        $menu_drop_selector.on(
            'touchstart', function (event) {
              $(this).menu_drop('show').
                  siblings(selected_state).
                  menu_drop('hide');
             //  Make all touch events stop at the menu so we don't hide the menu when we want click.
              event.stopPropagation();
              //make sure we close the menus when you click anywhere else, but only do it once so that the touchstart event is not bound for all eternity.
              $(document).one(
                  'touchstart', function () {
                    $menu_drop_selector.menu_drop('hide');
                  });
            });

      } else {
        //HoverIntent.js is used for the main navigation delay on hover
        $menu_drop_selector.hoverIntent(
            function () {$(this).menu_drop('show');},
            function () {$(this).menu_drop('hide');}
        );
      }
    }
  };
})(jQuery);