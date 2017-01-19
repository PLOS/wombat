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
 * Created by pgrinbaum on 8/22/14.
 */
;(function ($) {
  var s;
  toggle_component = {

    settings: {
      toggle_trigger: '[data-js-toggle=toggle_trigger]',
      toggle_target: '[data-js-toggle=toggle_target]',
      toggle_add: '[data-js-toggle=toggle_add]',
      speed: 0
    },

    init: function () {

      this.toggle();

    },

    toggle: function () {
      s = this.settings;
      $(s.toggle_trigger).on('click', function () {

        $(this).siblings(s.toggle_trigger).addBack().toggle(s.speed); //TODO: don't repeat myself so much here.
        $(this).siblings(s.toggle_target).toggle(s.speed); //TODO rewrite to not use jQuery
        $(this).siblings(s.toggle_add).toggle(s.speed);
      });
    }

  };


})(jQuery);