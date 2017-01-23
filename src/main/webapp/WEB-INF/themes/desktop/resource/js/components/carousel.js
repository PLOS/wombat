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
 * Created by pgrinbaum on 5/21/14.
 * using jcarousel
 */
  carousel = {

    settings: {
      container:       '.jcarousel',
      current_locator: '[data-js=carousel-current-item]',
      index_locator:   'span[data-js=carousel-total-index]',
      controls:        '.carousel-control',
      control_next:    '.jcarousel-next',
      control_prev:    '.jcarousel-prev'
    },

    init:    function () {
      // kick things off
      this.bind_actions();

    },
    numbers: function () {
      var s=this.settings;

      $(s.container)
        // get index
        .on('jcarousel:create',function (event, carousel) {
          var total_index = $(this).jcarousel('items').length;
          $(this).next(s.controls).find(s.index_locator).html(total_index);
          $(this).next(s.controls).find(s.current_locator).html('1');
        }).
        // change number
        on('jcarousel:animateend', function (event, carousel) {
          var current_item = $(this).jcarousel('visible').index();
          var current_item_readable = (current_item + 1);
          $(this).next(s.controls).find(s.current_locator).html(current_item_readable);
        })
         /// initialise jcarousel
        .jcarousel({wrap: 'both'});
    },

    next_prev: function () {
      var s=this.settings;

      $(s.control_prev).jcarouselControl({
        target: '-=1'
      });

      $(s.control_next).jcarouselControl({
        target: '+=1'
      });
    },

    bind_actions: function () {
      this.numbers();
      this.next_prev();
    }

  };


