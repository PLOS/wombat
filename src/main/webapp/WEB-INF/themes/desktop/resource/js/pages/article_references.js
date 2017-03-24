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
  referenceValidation = Class.extend({

    init: function () {

      this.getJSON();

    },


    getJSON: function () {
      that = this;

      $('.references li').on('click', 'ul.reflinks li:first-child a', function (event) {

        var queryStringAuthor = $(this).attr('data-author');
        var queryStringTitle = $(this).attr('data-title');
        var queryStringCit = $(this).attr('data-cit');
        var doiAvailableText = 'doi-provided';

        if (queryStringCit !== doiAvailableText) {

          var $that = $(this);

          var queryStringConcat = 'query.author=' + queryStringAuthor + '&query.title=' + queryStringTitle;
          var crossrefApi = "http://api.crossref.org/works?query=" + queryStringConcat + "&sort=score&rows=1";
          var DOIResolver = 'https://doi.org/';
          var crossrefSearchString = 'http://search.crossref.org/?q=' + queryStringCit;
          var articleLink = null;


          event.preventDefault();

          $.ajax({
                url: crossrefApi,
                beforeSend: (
                    function () {
                      $that.addClass('link-disabled');
                    })
              })
              .success(
                  function (data) {
                    var DOIs = data.message.items[0].DOI;
                    articleLink = DOIResolver + DOIs;
                  }
              )
              .error(
                  function () {
                    articleLink = crossrefSearchString;
                  }
              )
              .done(function () {
                window.open(articleLink, '_new');
                $that.removeClass('link-disabled');
              });

        };
      });

    },


  });
  this.articleReferences = new referenceValidation();

})(jQuery);








