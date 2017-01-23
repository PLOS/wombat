/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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

      if ($('html.touch:not(.desktop)').length) {
        $menu_drop_selector.on(
            'touchstart', function (event) {
              $(this).menu_drop('show').
                  siblings(selected_state).
                  menu_drop('hide');

             //  Make all touch events stop so we don't hide the menu when we want click.
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