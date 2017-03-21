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
  var cut_tweet = (function() {
    var get_text, parts, after_text, count_text, cutit, mod_cut, newhref;
    get_text = $('#twitter-share-link').attr('href');
    //split after '&' in '&text='
    parts = get_text.split('&');
    after_text = parts[1].substring(5);
    count_text = after_text.length;

    if (count_text > 115) {
      cutit = '&text=' + after_text.substring(0, 115) + ' ...';
      //replace semicolons
      mod_cut = cutit.replace(/;/g,'%3B');
      newhref = parts[0] + mod_cut;
      return $('#twitter-share-link').attr('href', newhref);
    }
  })();

})(jQuery);