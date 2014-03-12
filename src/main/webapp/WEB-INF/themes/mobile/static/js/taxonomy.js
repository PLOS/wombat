// Javascript controlling the taxonomy browser.

var TaxonomyBrowser = function () {
  var self = this;

  self.init = function () {
    self.$browserDiv = $('#browse-container');
    if (self.$browserDiv.length) {
      var term = self.getTermFromUrl();
      self.pushState(term);

      // Upon initial loading, this also loads the list of top-level terms.
      $(window).bind('popstate', function(e) {
        var term = self.getTermFromUrl();
        if (term) {
          term = '/' + term;
        }
        self.loadTerms(term, false);
      });
    }
  };

  // Examines window.location to see if we are starting at a given term; if so, returns that term.
  // If we are at the top level, returns null.
  self.getTermFromUrl = function() {

    // TODO: this will break if we ever have more than one parameter on this page.
    var pieces = window.location.href.split('?path=');
    var term = null;
    if (pieces.length == 2) {
      term = decodeURI(pieces[1]);
    }
    return term;
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
      self.pushState(parent);
    }
  };

  // Saves the current state to the browser history, so the back button functions correctly.
  // Term should be the parent term of the ones we are currently displaying.
  self.pushState = function(term) {
    var url = 'browse';
    var title = 'browse';
    if (term) {
      url += '?path=' + term;
      title = term;
    }
    window.history.pushState(null, title, url);
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
        console.log('Error loading term ' + parent + ': ' + textStatus);
      }
    });
  };
};

$(document).ready(function () {
  var browser = new TaxonomyBrowser();
  browser.init();
});
