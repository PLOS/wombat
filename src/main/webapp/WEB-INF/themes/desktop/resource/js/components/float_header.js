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
 * Created by ddowell on 9/16/14.
 * DEPENDENCY: resource/js/components/show_onscroll
 */
var s, float_header = {   // TODO: make extensible so floater, hidden_div, & scroll_trigger can be set as options
  settings: {
    floater : $("#floatTitleTop"), // TODO: use data-js-floater as selector
    hidden_div : "topVisible",
    scroll_trigger : 420,
    div_exists : 1
  },

  init: function () {
    s = this.settings;
    this.scroll_stuff();
    this.close_floater();
  },

  check_div : function () {
    s.div_exists = s.floater.length;
    return s.div_exists;
  },

  scroll_stuff :  function () {
    if (this.check_div() > 0) {

      return $(window).on('scroll', function () {
        //show_onscroll is in resource/js/components/
        var show_header = show_onscroll(s.floater, s.hidden_div, s.scroll_trigger);

      });
    }
  },

  close_floater : function () {
    s.floater.find('.logo-close').on('click', function () {
      s.floater.remove();
    });
  }
};
