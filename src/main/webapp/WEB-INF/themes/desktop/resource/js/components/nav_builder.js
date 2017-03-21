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

(function ($) {
  $.fn.buildNav = function (options) {
    var defaults = {
      content: '',
      margin: 70
    };
    var options = $.extend(defaults, options);
    return this.each(function () {
      var $this = $(this),
          $new_ul = $('<ul class="nav-page" />'),
          $anchors = (options.content).find('a[data-toc]');
      if ($anchors.length > 0) {
        $anchors.each(function () {
          var this_a = $(this),
            title = this_a.attr('title'),
            target = this_a.attr('data-toc'),
            itemClass = this_a.attr('id');

          $('<li><a href="#' + target + '" class="scroll">' + title + '</a></li>').addClass(itemClass).appendTo($new_ul);
        });
        $new_ul.find('li').eq(0).addClass('active');

        $new_ul.prependTo($this);
        $this.on("click", "a.scroll", function (event) {
          var link = $(this);

          //window.history.pushState is not on all browsers
          if (window.history.pushState) {
            window.history.pushState({}, document.title, event.target.href);
          } else {  }

          event.preventDefault();
          $('html,body').animate({scrollTop: $('[name="' + this.hash.substring(1) + '"]').offset().top - options.margin}, 500, function () {
            // see spec
            // window.location.hash = link.attr('href');
          });
        });
      };

    });
  };
})(jQuery);
