
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

/*doc
 ---
 title: truncate elements
 name: truncate_elem
 category: js widgets
 ---

 ---

 pass the container element for what needs truncating

 used on article.js for floating header authors and on figviewer.js for header authors
 truncate_elem.remove_overflowed('#floatAuthorList');

*/
;(function ($) {
  truncate_elem = {

   //TODO: add options for the trunc ending
    remove_overflowed : function (to_truncate) {
      var elem_list_height, trunc_ending, elem_list, remove_list, get_last;
      trunc_ending = "&hellip;";
      elem_list = $(to_truncate);
      remove_list = [];
      elem_list_height = $(elem_list[0]).offset().top;

      elem_list.children().each(function () {
        if ($(this).offset().top > elem_list_height ) {
          remove_list.push(this);
          return  $(this).addClass("remove");
        }
      });

      get_last = remove_list[0];
      $(get_last).prev().append(trunc_ending);
      $(".remove").css('visibility','hidden');

    }

  }

})(jQuery);

