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

// TaxonomyBrowser requires jquery-mousewheel
// https://github.com/brandonaaron/jquery-mousewheel

/*! Copyright (c) 2013 Brandon Aaron (http://brandonaaron.net)
 * Licensed under the MIT License (LICENSE.txt).
 *
 * Thanks to: http://adomas.org/javascript-mouse-wheel/ for some pointers.
 * Thanks to: Mathias Bank(http://www.mathias-bank.de) for a scope bug fix.
 * Thanks to: Seamus Leahy for adding deltaX and deltaY
 *
 * Version: 3.1.3
 *
 * Requires: 1.2.2+
 */

(function (e) {
  "function" == typeof define && define.amd ? define(["jquery"], e) : "object" == typeof exports ? module.exports = e : e(jQuery)
})(function (e) {
  function t(t) {
    var l, i = t || window.event, s = [].slice.call(arguments, 1), h = 0, u = 0, a = 0, r = 0, d = 0;
    return t = e.event.fix(i), t.type = "mousewheel", i.wheelDelta && (h = i.wheelDelta), i.detail && (h = -1 * i.detail), i.deltaY && (a = -1 * i.deltaY, h = a), i.deltaX && (u = i.deltaX, h = -1 * u), void 0 !== i.wheelDeltaY && (a = i.wheelDeltaY), void 0 !== i.wheelDeltaX && (u = -1 * i.wheelDeltaX), r = Math.abs(h), (!o || o > r) && (o = r), d = Math.max(Math.abs(a), Math.abs(u)), (!n || n > d) && (n = d), l = h > 0 ? "floor" : "ceil", h = Math[l](h / o), u = Math[l](u / n), a = Math[l](a / n), s.unshift(t, h, u, a), (e.event.dispatch || e.event.handle).apply(this, s)
  }

  var o, n, l = ["wheel", "mousewheel", "DOMMouseScroll", "MozMousePixelScroll"], i = "onwheel"in document || document.documentMode >= 9 ? ["wheel"] : ["mousewheel", "DomMouseScroll", "MozMousePixelScroll"];
  if (e.event.fixHooks)for (var s = l.length; s;)e.event.fixHooks[l[--s]] = e.event.mouseHooks;
  e.event.special.mousewheel = {
    setup: function () {
      if (this.addEventListener)for (var e = i.length; e;)this.addEventListener(i[--e], t, !1); else this.onmousewheel = t
    }, teardown: function () {
      if (this.removeEventListener)for (var e = i.length; e;)this.removeEventListener(i[--e], t, !1); else this.onmousewheel = null
    }
  }, e.fn.extend({
    mousewheel: function (e) {
      return e ? this.bind("mousewheel", e) : this.trigger("mousewheel")
    }, unmousewheel: function (e) {
      return this.unbind("mousewheel", e)
    }
  })
});

(function ($) {
  (function TaxonomyBrowser() {

  var ANIMATION_TIME = 200; // in ms
  var API_URL = siteUrlPrefix + 'taxonomy/';
  var SEARCH_URL = $('#taxonomy-browser').attr('data-search-url') + '/';

  // store the term as a key with its children terms as an array of strings,
  // emulating the same data structure as the API response. the key '/'
  // holds top-level terms.
  var term_cache = {
    // 'term' : ['array', 'of', 'child', 'terms']
  };

  // array that terms will get push()'d on to, representing the taxonomy
  // browser's current state. note: the first item is always "/"
  var term_stack = [
    // "parent term", "child term", "grandchild term", "great-grandchild term"
  ];

  // Maps subject terms to article count for display in the menus
  var term_counts = {};

  // Maps subject terms to child term count for determining if term is a leaf in the taxonomy tree
  var child_counts = {};

  // holds a DOM reference to the last active column
  var $last_column_active = null;

  // holds a timeout ref used when turning columns on and off
  var column_timeout = null;

  // holds the width of the column for animation calculations
  var column_width = null;

  //We must support a click as well as a mousedown hold to scroll through the taxonomy browser
  //This flag is checked to see if the "click" action can happen
  var clickIsValid = true;

  //milliseconds allowed after the start of a mousedown event for the click action to fire
  var delay = 100;

  var clickAnimationSpeed = 300;

  var clickAnimationAmount = 100;

  var dontClick = function(){
    clickIsValid = false;
  };

  // MAIN FUNCTIONS ==========================================================

  /**
   * Return the children for a given term.
   */
  function getChildren(term) {

    loadTerms(term);

    if ((typeof(term_cache[term]) != 'undefined')) {
      return term_cache[term];
    } else {
      return [];
    }
  }

  /**
   * Displays the given term at the given level
   */
  function displayTerm(term, level) {
    // if we clicked a lower-level item (ignore the root term in the stack for length calculations)
    if (level <= (term_stack.length - 1)) {
      removeTermsAboveLevel(level);
    }

    var child_terms = getChildren(term);
    term_stack.push(term);
    displayChildren(term, child_terms);
  }

  /**
   * a one-stop shop for displaying the children of the clicked term,
   * handling rendering, left/right arrow logic, etc.
   * once the data has been requested and made available to us.
   */
  function displayChildren(term, child_terms) {

    // generate new child list based term_cache keying off last item in term_stack
    renderChildren(term, child_terms);

    var current_depth = $('.levels-position .level').length;
    var left_position = $('.levels-position').position().left;
    var hidden_left_cols = Math.abs(left_position / column_width);
    var hidden_right_cols = current_depth - (3 /* visible columns */) - hidden_left_cols;

    // slide the carousel over if there's no room to the right
    if ((hidden_right_cols > 0)) {
      animateCarousel('-=' + (column_width * hidden_right_cols));
    }
  }

  /**
   * Renders the list for the given children
   * @param term the parent term
   * @param child_terms an array of terms, taken from the term_cache
   */
  function renderChildren(term, child_terms) {

    function createDataObject(terms_array) {
      // loop through each item to see if they have children. Build out object data for the template
      return $.map(terms_array, function (term) {
        if (term === 'ROOT') {
          return;
        }
        return {
          'name': term,
          'isLeaf': child_counts[term] === 0,
          'link': buildSubjectUrl(term),
          'count': term_counts[term]
        };
      });
    }

    if (term === '/') {
      term = 'ROOT';
    }

    var markup = buildColumnMarkup({
      parent_term: term,
      items: createDataObject(child_terms),
      level: term_stack.length, // level is 1-based, array length is 0-based
      view_all_link: buildSubjectUrl(term),
      view_all_total: term_counts[term]
    });

    $('.levels-position').append(markup);
    turnColumnOn($('.level').last());
  }

  // EVENT HANDLERS ==========================================================

  /**
   * Handles the click event on terms in columns (levels)
   */
  function handleTermClick(event) {
    var clicked_el = $(event.target);

    // bail out if the term is a leaf node, so the link can go through normally
    if (clicked_el.hasClass('no-children')) {
      return true;
    }

    event.preventDefault();

    var clicked_level_num = clicked_el.data('level');
    displayTerm(getTermFromElement(clicked_el), clicked_level_num);

    var closest_level_el = clicked_el.closest('.level');
    closest_level_el.find('li').removeClass('active');
    clicked_el.closest('li').addClass('active');
  }

  /**
   * Click handler for the left/right carousel buttons
   */
  function handleCarouselClick(event) {
    event.preventDefault();

    var clicked_el = $(event.target);
    //do not trigger an event if the carousel is currently animating
    if (!clicked_el.hasClass('active') || $('.levels-position').is(':animated')) {
      return false;
    }

    var operator = (clicked_el.hasClass('next')) ? '-=' : '+=';
    var delta = operator + $('.level').outerWidth(true);

    animateCarousel(delta);

    //carousel buttons are links and will receive focus after clicking, breaking the styling.
    //todo: create a :focus & :active override in the CSS to handle this case
    $(':focus').blur();
  }

  /**
   * Handles scrolling the term list
   */
  function handleScrollColumnMousedown(event) {
    event.preventDefault();

    cancelClick = setTimeout( dontClick, delay );

    var el = $(event.target);
    var column = el.siblings('.level-scroll');
    var column_height = column.children('ul').height();
    var column_scroll_top = column.scrollTop();

    // timing needs to be dynamic to keep speed somewhat constant for shorter distances
    var animation_time = 800;
    var remaining_distance = el.hasClass('up') ? column_scroll_top : column_height - column_scroll_top;
    animation_time = Math.round(remaining_distance * animation_time / column_height);

    column.animate({
      'scrollTop': el.hasClass('up') ? 0 : column_height
    }, animation_time);
  }

  function handleScrollColumnMouseup(event) {
    event.preventDefault();

    var el = $(event.target);
    var column = el.siblings('.level-scroll');

    clearTimeout(cancelClick);
    column.stop();

    if (clickIsValid) {
      var y = column.scrollTop();
      column.animate({
        'scrollTop': el.hasClass('up') ? y - clickAnimationAmount : y + clickAnimationAmount
      }, clickAnimationSpeed);
    }

    clickIsValid = true;
  }

  /**
   * Handles the scrolling of the mouse wheel
   */
  function handleMousewheel(event, delta, deltaX, deltaY) {
    event.preventDefault();

    // normalize for acceleration, if present. only allow for +/-1 delta,
    // forcing a constant scroll speed.
    //
    // the delta is divided by a high-ish number that, usually puts it
    // between the bounds of -1 and 1 so we can floor/ceil it to +/- 1. if
    // the delta is above the number, that's okay. the user probably wants to
    // scroll faster, so we'll let them.
    //
    // inspired by v3.0.6 of jquery-mousewheel and issue #36:
    // <https://github.com/brandonaaron/jquery-mousewheel/issues/36>.
    //
    // NOTE: we're using deltaY here as delta and deltaY sometimes differ,
    // and deltaY seems to be the better choice. probably has something to do
    // with trackpads and x/y scrolling.
    var normalized_delta = Math[(deltaY < 0 ? 'floor' : 'ceil')](deltaY / 60);
    var distance_to_scroll = normalized_delta * 10;
    var scrollTop = $(this).scrollTop();
    $(this).scrollTop(scrollTop - distance_to_scroll);
  }

  // UI FUNCTIONS ============================================================

  /**
   * Pans the carousel left and right
   * @param  {string} delta The parameter that gets passed to the actual animate function,
   * which is either number or an offset (+/-=)
   */
  function animateCarousel(delta) {
    $('.levels-position').stop().animate({
      'left': delta
    }, ANIMATION_TIME, updateCarouselButtons);
  }

  /**
   * Determines if the carousel previous and next buttons should display
   */
  function updateCarouselButtons() {
    var left_position = Math.round($('.levels-position').position().left);
    var current_depth = $('.levels-position .level').length;
    var hidden_left_cols = Math.abs(left_position / column_width);
    var hidden_right_cols = current_depth - 3 - hidden_left_cols;

    var prev_button = $('.prev');
    var next_button = $('.next');

    if (hidden_left_cols > 0) {
      prev_button.show().addClass('active');
    } else {
      prev_button.hide().removeClass('active');
    }

    if (hidden_right_cols > 0) {
      next_button.show().addClass('active');
    } else {
      next_button.hide().removeClass('active');
    }

  }

  /**
   * Handles the hovering over each column
   *
   * interaction notes:
   *
   *  - only one column may be "level-active" (light gray) at a time. all
   *    other columns are "level-selection"
   *
   *  - a level becomes "active" when either its parent is clicked (thereby
   *    displaying it), or by hovering over it (non-touch devices only)
   *
   *  - hovering over an "active" column additionaly shows the up/down scroll
   *    arrows (non-touch devices only)
   *
   *  - the way the CSS rules are written, "level-selection" and
   *    "level-active(-over)" are mutually exclusive. (they cannot be applied
   *    at the same time.)
   *
   *  - therefore, when we hover over a "level-selection" column (thereby
   *    making it "level-active"), we have to remove the "level-selection"
   *    class and add "level-active level-active-over" and turn off
   *    "level-active-(over)" on all other columns
   *
   *  - it may be easier to just (re)set all columns all the time. the rules
   *    are pretty simple: the last column is "level-active"
   */
  function turnColumnOff(el, requester) {

    clearTimeout(column_timeout);
    el.removeClass('level-active-over');

    var isNotLastLevel = (el.data('level') <= term_stack.length);
    var isLowerColumnActive = (requester && (requester.data('level') < el.data('level')));
    if (isNotLastLevel || isLowerColumnActive) {
      el.removeClass('level-active');
      el.addClass('level-selection');
    }

    $last_column_active = null;
  }

  function turnColumnOn(el) {

    clearTimeout(column_timeout);

    if ($last_column_active !== null && $last_column_active.data('level') !== el.data('level')) {
      turnColumnOff($last_column_active, el);
    }

    el.removeClass('level-selection');
    el.addClass("level-active level-active-over");

    $last_column_active = el;
  }

  // UTLITY FUNCTIONS ========================================================

  function buildColumnMarkup(data) {
    var terms = $.map(data.items, function (item, idx) {
      return [
        '<li>',
        '<a href="' + item.link + '"' + (item.isLeaf ? ' class="no-children"' : '') + ' data-level="' + data.level + '">',
        item.name + ' ' + (item.isLeaf ? '(' + item.count + ')' : ''),
        '</a>',
        '</li>'
      ].join("\n");
    });

    var parentTerm = data.parent_term;
    if (parentTerm === 'ROOT') {
      parentTerm = 'All Subject Areas';
    }

    var markup = [
      '<div class="level" data-level="' + data.level + '">',
      '<div class="level-title">' + parentTerm + '</div>',
      '<div class="level-top">',
      '<a href="' + data.view_all_link + '">View All Articles (' + data.view_all_total + ')</a>',
      '</div>',
      '<a href="#" class="up"></a>',
      '<div class="level-scroll">',
      '<ul>',
      terms.join("\n"),
      '</ul>',
      '</div>',
      '<a href="#" class="down"></a>'];

    //We don't need the up / down arrows for less then 5 items
    if (data.items.length < 5) {
      markup[5] = '<a href="#" class="up hide-active-scroll"></a>';
      markup[11] = '<a href="#" class="down hide-active-scroll"></a>';
    }

    markup.push(['</div>']);

    return markup.join("\n");
  }

  /**
   * Build out the api url string based on which items are in the collection
   */
  function buildSubjectUrl(last_term) {

    var url = SEARCH_URL;

    //Replace all spaces with "_" and encode the special characters
    if ((typeof(last_term) !== 'undefined') && (last_term !== 'ROOT')) {
      last_term = "" + last_term;
      url = url + encodeURIComponent(last_term.replace(new RegExp("\\s", 'g'), "_").toLowerCase());
    }

    return url;
  }

  // get the term from the markup and trim it of whitespace and child count
  function getTermFromElement(el) {
    return $.trim(el.html().replace(/\([0-9]+\)/g, ""));
  }

  /**
   * Makes API request for child terms for the given term
   */
  function loadTerms(parent_term) {

    function handleSuccess(terms, textStatus, xhr) {

      var child_terms = [];
      for (var i = 0; i < terms.length; i++) {
        var fullPath = terms[i].subject;
        var levels = fullPath.split('/');
        var leaf = levels[levels.length - 1];
        child_terms.push(leaf);

        var articleCount = terms[i].articleCount;
        if (articleCount != undefined && articleCount > 0) {
          term_counts[leaf] = articleCount;
        } else {
          term_counts[leaf] = 0;
        }

        var childCount = terms[i].childCount;
        if (childCount != undefined && childCount > 0) {
          child_counts[leaf] = childCount;
        } else {
          child_counts[leaf] = 0;
        }

        term_cache[leaf] = [];
      }

      term_cache[parent_term] = child_terms.sort();
    }

    function handleFailure(jqXHR, textStatus, errorThrown) {

      $('.subject-area').unbind('click', displayBrowser)
        .attr("title", "Error: No subject terms available!")
        .addClass("disabled");

      $('subject-area').on('click', function (e) {
        e.preventDefault();
        return false;
      });
    }

    var url = createUrlFromTermStack();
    if (parent_term != '/') {
      url += "c=" + parent_term;
    }

    //Replace spaces with underscores, will be reverted to spaces in TaxonomyController.
    //This prevents 502 proxy errors that occur when we try to request a url with '%20' in it.
    //todo: After cleaning up redirects and solving the 502 proxy error, this should be removed
    url = url.replace(/\s/g, "_");

    $.ajax({
      url: url,
      type: 'GET',
      dataType: 'json',
      async: false
    }).then(handleSuccess, handleFailure);

    return true;
  }

  function createUrlFromTermStack() {
    var url = API_URL + "?";
    for (var i = 1; i < term_stack.length; i++) {
      url += "c=" + term_stack[i] + "&";
    }
    return url;
  }

  function removeTermsAboveLevel(dest_level) {

    while ($('.levels .level').length > dest_level) {
      $('.levels .level').last().remove();
      term_stack.pop();
    }
  }

  function toggleTaxonomyBrowser(force_close) {
    // bail if we're already animating
    if ($tb.queue().length > 0) {
      return;
    }

    force_close = force_close || false;

    var tb_closed = $tb.is(':hidden');

    if (force_close || !tb_closed) {
      if (!tb_closed) {
        $tb.slideUp();
      }
    } else {
      $tb.slideDown();
    }
  }

  function displayBrowser(event) {
    event.preventDefault();
    event.stopPropagation();

    toggleTaxonomyBrowser();
  }

  function attachEventHandlers() {

    $('.level-active').hover(function () {
      $(this).addClass('level-active-over');
    }, function () {
      $(this).removeClass('level-active-over');
    });

    $('.subject-area').on('click', displayBrowser);

    $(document).bind('click', function (event) {
      toggleTaxonomyBrowser(true);
    });

    $tb.click(function (event) {
      event.stopPropagation();
    });

    var $levelsPosition = $('.levels-position');

    // no column hover events for touch devices
    if (!(('ontouchstart' in window) || window.DocumentTouch && document instanceof DocumentTouch)) {
      $levelsPosition.on('mouseenter', '.level', function (e) {
        turnColumnOn($(e.currentTarget));
      });

      $levelsPosition.on('mousedown', '.level > a', handleScrollColumnMousedown);
      $levelsPosition.on('mouseup', '.level > a', handleScrollColumnMouseup);
    }

    $levelsPosition.on('mousewheel', '.level-scroll', handleMousewheel);

    $levelsPosition.on('click', '.level > a', function (e) {
      e.preventDefault();
    });

    $levelsPosition.on('click', 'ul li a', handleTermClick);

    $('#taxonomy-browser').find('.prev, .next').click(handleCarouselClick);
  }

  $(document).ready(function () {

    $tb = $('#taxonomy-browser');

    attachEventHandlers();
    displayTerm('/', 0 /*level*/);

    column_width = Math.round($('.levels-position .level').outerWidth(true));
  });
})()
})(jQuery);
