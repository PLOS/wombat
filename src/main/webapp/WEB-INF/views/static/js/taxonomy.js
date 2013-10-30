
// Javascript controlling the taxonomy browser.

var TaxonomyBrowser = function() {
  var self = this;

  self.init = function() {
    self.$browserDiv = $('#browse-container');
    self.loadTerms(null);
  };

  // Renders the browser, given a JSON list of subjects.
  self.renderTerms = function(terms) {
    var termList = '';
    for (var i = 0; i < terms.length; i++) {
      var fullPath = terms[i].subject;
      var levels = fullPath.split('/');
      var leaf = levels[levels.length - 1];
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
    $('a.browse-further').click(function(e) {
      self.loadTerms($(this).data('term'));
    });
  };

  // Loads the child terms given a parent term.  If the parent evaluates to false,
  // the root taxonomy terms will be loaded.
  self.loadTerms = function(parent) {
    var url = 'taxonomy';
    if (parent) {
      url += parent;
    } else {
      url += '/';
    }
    $.ajax(url, {
      type: 'GET',
      success: function(data) {
        self.renderTerms(data);
      },
      error: function(xOptions, textStatus) {
        console.log(textStatus);
      }
    });
  };
};

$(document).ready(function () {
  browser = new TaxonomyBrowser();
  browser.init();
});
