
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

//var s;

(function ($) {

  //Setting up global foundation settings - you can override these the documentation is here:
// http://foundation.zurb.com/docs/javascript.html#configure-on-the-fly
  $(document).foundation({

    //Tooltips
    tooltip: {
      'wrap': 'word',
      // 'disable_for_touch': 'true',
      tip_template: function (selector, content) {
        return '<span data-selector="' + selector + '" class="'
            + Foundation.libs.tooltip.settings.tooltip_class.substring(1)
            + '">' + content + '</span>';
      }
    },
    // reveal is used for the figure viewer modal
    reveal: {
      animation: false
    }

  });

  var searchCheck = function (){

    if (!Modernizr.input.required) {
      $("form[name='searchForm']").submit(function() {
        var searchTerm = $("#search").val();
        if (!searchTerm) return false;
      });
    }

  };

  $(document).ready(function () {
    var runSearchCheck = searchCheck();

    // hover delay for menu
    hover_delay.init();

    //placeholder style change
    placeholder_style.init();

    // initialize tooltip_hover for everything
    tooltip_hover.init();

  });
})(jQuery);