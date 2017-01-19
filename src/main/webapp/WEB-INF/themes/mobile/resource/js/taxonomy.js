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

// Javascript controlling the taxonomy browser.

var TaxonomyBrowser = function () {
  var self = this;

  self.init = function () {
    self.$browserDiv = $('#browse-container');

    if (self.$browserDiv.length) {
      var term = self.getTermFromUrl();
      self.loadTerms(term, false);

      $(window).bind('popstate', function (e) {
        var term = self.getTermFromUrl();
        self.loadTerms(term, false);
      });
    }
  };

  // Examines window.location to see if we are starting at a given term; if so, returns that term.
  // If we are at the top level, returns null.
  self.getTermFromUrl = function() {

    // TODO: this will break if we ever have more than one parameter on this page.
    var pieces = window.location.href.split('?path=');
    var term = '/';
    if (pieces.length == 2) {
      term = decodeURI(pieces[1]);
    }
    return term;

  };

  // Renders the browser, given a JSON list of subjects.
  // If pushState is true, we will push the current state onto the browser history.
  self.renderTerms = function (terms, pushState) {
    var termList = '';
    var parent = '/';
    for (var i = 0; i < terms.length; i++) {
      var fullPath = terms[i].subject;
      var levels = fullPath.split('/');
      var leaf = levels[levels.length - 1];
      if (leaf == 'ROOT') {
        break;
      }
      if (parent == '/') {
        parent += levels[0];
      }

      var termHtml = $('#subject-term-template').html();
      termHtml = termHtml.replace('__TAXONOMY_TERM_ESCAPED__', leaf.replace(/ /g, '_'));
      termHtml = termHtml.replace('__TAXONOMY_TERM_LEAF__', leaf);
      termHtml = termHtml.replace('__TAXONOMY_TERM_FULL_PATH__', fullPath);
      var childLinkStyle = 'browse-further browse-right';
      if (terms[i].childCount === 0) {
        childLinkStyle += ' inactive';
      }
      termHtml = termHtml.replace('__CHILD_LINK_STYLE__', childLinkStyle);
      termList += termHtml;
    }
    var html = $('#subject-list-template').html();
    html = html.replace('__TAXONOMY_LINKS__', termList);
    $('#browse-container').html(html);
    $('a.browse-further:not(.inactive)').click(function (e) {
      self.loadTerms($(this).data('term'), true);
    });

    if (pushState) {
      self.pushState(parent);
      $('body').addClass('historyPushed');
    }
  };

  // Saves the current state to the browser history, so the back button functions correctly.
  // Term should be the parent term of the ones we are currently displaying.
  self.pushState = function(term) {
    var url = 'subjectAreaBrowse';
    var title = 'subjectAreaBrowse';
    if (term) {
      url += '?path=' + term;
      title = term;
    }
    window.history.pushState(null, title, url);
  };

  // Loads the child terms given a parent term.  If the parent evaluates to false,
  // the root taxonomy terms will be loaded.
  self.loadTerms = function (parent, pushState) {

    var url = 'taxonomy/?';
    if (parent != '/') {
      var termArray = parent.split('/');
      var lastTerm = termArray[termArray.length -1];
      url += 'c=' + lastTerm;
    }

    //Replace spaces with underscores, will be reverted to spaces in TaxonomyController.
    //This prevents 502 proxy errors that occur when we try to request a url with '%20' in it.
    //todo: After cleaning up redirects and solving the 502 proxy error, this should be removed
    url = url.replace(/\s/g, "_");

    $.ajax(url, {
      type: 'GET',
      success: function (data) {
        self.renderTerms(data, pushState);
      },
      error: function (xOptions, textStatus) {
        console.log('Error loading term ' + parent + ': ' + textStatus);
      }
    });
  };
};

$(document).ready(function () {
  var browser = new TaxonomyBrowser();
  browser.init();
});
