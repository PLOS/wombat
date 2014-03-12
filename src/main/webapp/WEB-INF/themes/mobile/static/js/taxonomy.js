// Javascript controlling the taxonomy browser.

var TaxonomyBrowser = function () {
  var self = this;

  self.init = function () {
    self.$browserDiv = $('#browse-container');
    if (self.$browserDiv.length) {

      // Play nicely with the back button.
      window.history.pushState(null, 'browse', 'browse');

      // Upon initial loading, this also loads the list of top-level terms.
      $(window).bind('popstate', function(e) {
        var pieces = window.location.href.split('?path=');
        var term = null;
        if (pieces.length == 2) {
          term = '/' + decodeURI(pieces[1]);
        }
        self.loadTerms(term, false);
      });
    }
  };

  // Renders the browser, given a JSON list of subjects.
  // If pushState is true, we will push the current state onto the browser history.
  self.renderTerms = function (terms, pushState) {
    var termList = '';
    for (var i = 0; i < terms.length; i++) {
      var fullPath = terms[i].subject;
      var levels = fullPath.split('/');
      var leaf = levels[levels.length - 1];

      // Get parent term if there is one.  Only do this once for efficiency since all terms
      // will have the same parent.
      if (i == 0 && levels.length > 2) {
        var parent = '';
        for (var j = 0; j < levels.length - 1; j++) {
          if (j > 1) {
            parent += '/';
          }
          parent += levels[j];
        }
      }

      var termHtml = $('#subject-term-template').html();
      termHtml = termHtml.replace('__TAXONOMY_TERM_ESCAPED__', encodeURIComponent(leaf));
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
      var url = 'browse';
      var title = 'browse';
      if (parent) {
        url += '?path=' + parent;
        title = parent;
      }
      window.history.pushState(null, title, url);
    }
  };

  // Loads the child terms given a parent term.  If the parent evaluates to false,
  // the root taxonomy terms will be loaded.
  self.loadTerms = function (parent, pushState) {
    var url = 'taxonomy';
    if (parent) {
      url += parent;
    } else {
      url += '/';
    }
    $.ajax(url, {
      type: 'GET',
      success: function (data) {
        self.renderTerms(data, pushState);
      },
      error: function (xOptions, textStatus) {
        console.log(textStatus);
      }
    });
  };
};

$(document).ready(function () {
  var browser = new TaxonomyBrowser();
  browser.init();
});
