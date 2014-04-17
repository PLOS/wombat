/**
 * Copyright (c) 2007-2013 by Public Library of Science
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
  e.event.special.mousewheel = {setup: function () {
    if (this.addEventListener)for (var e = i.length; e;)this.addEventListener(i[--e], t, !1); else this.onmousewheel = t
  }, teardown: function () {
    if (this.removeEventListener)for (var e = i.length; e;)this.removeEventListener(i[--e], t, !1); else this.onmousewheel = null
  }}, e.fn.extend({mousewheel: function (e) {
    return e ? this.bind("mousewheel", e) : this.trigger("mousewheel")
  }, unmousewheel: function (e) {
    return this.unbind("mousewheel", e)
  }})
});


// ===========================================================================


(function TaxonomyBrowser() {

  // "GLOBAL" VARIABLES ======================================================

  // constant used for various animations
  var ANIMATION_TIME = 200; // in ms

  // URL of the taxonomy API
  var API_URL = '/taxonomy/json/'; // trailing slash is required
  var REST_URL = '/browse/';

  // store the term as a key with its children terms as an array of strings,
  // emulating the same data structure as the API response. the key '/'
  // holds top-level terms.
  //
  // NOTE: that this is sort of deprecated and not well-used anymore.
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

  // holds the main element of the taxonomy browser, assigned in initialize()
  var $el = null;

  // holds a DOM reference to the last active column
  var $last_column_active = null;

  // holds a timeout ref used when turning columns on and off
  var column_timeout = null;

  // holds the width of the column for animation calculations
  var column_width = null;


  // MAIN FUNCTIONS ==========================================================

  /**
   * Return the children for a given term.
   */
  function getChildren(term) {
    // console.log("getChildren: " + term);
    // if the item does not exist in the cache, make ajax call to api
    // all terms should be in the cache, even those with no children (empty array).
    // if ( typeof(term_cache[term]) == 'undefined') {
    loadTerms(term);
    // }

    // console.log("term_cache now: ", term_cache);

    if((typeof(term_cache[term]) != 'undefined')) {
      return term_cache[term];
    } else {
      return [];
    }
  }


  /**
   * Displays the given term at the given level
   */
  function displayTerm(term, level) {
    if(level > 0) {
      if(typeof(_gaq) !== 'undefined'){
        _gaq.push(['_trackEvent',"Taxonomy Browser", "Subject Clicked", term]);
      }
    }

    // if we clcked a lower-level item
    // (ignore the root term in the stack for length calcs)
    if (level <= (term_stack.length - 1)) {
      // console.log('clicked lower- or equal-level term; removing terms...');
      removeTermsAboveLevel(level);
    }
    else {
      // console.log("no need to remove terms; will add children as last level");
    }

    // catch unknown terms for debugging purposes
    if (typeof(term_cache[term]) == 'undefined') {
      // console.log('term "' + term + '" not in cache.');
    }

    var child_terms = getChildren(term);
    // console.log("children to display: " + child_terms.join(", "));

    term_stack.push(term);
    // console.log("pushed " + term + " onto term_stack (" + term_stack.join(" -> ") + ")");

    displayChildren(term, child_terms);
  }


  /**
   * a one-stop shop for displaying the children of the clicked term,
   * handling breadcrumb updating, rendering, left/right arrow logic, etc.
   * once the data has been requested and made available to us.
   */
  function displayChildren(term, child_terms) {
    // update breadcrumbs
    generateBreadcrumbs();

    // generate new child list based term_cache keying off last item in term_stack
    // renderChildren(term_cache[term_stack[term_stack.length - 1]]);
    renderChildren(term, child_terms);

    // get some metrics about our current display state (before animation)
    var left_position = $('.levels-position').position().left;
    var current_depth = $('.levels-position .level').length;
    var hidden_left_cols = Math.abs(left_position / column_width);
    var hidden_right_cols = current_depth - (3 /* visible columns */) - hidden_left_cols;

    // console.log([
    // 	"POSITION BEFORE ANIMATION",
    // 	"current_depth = " + current_depth,
    // 	"hidden_left_cols = " + hidden_left_cols,
    // 	"hidden_right_cols = " + hidden_right_cols
    // ].join("\n"));

    // slide the carousel over if there's no room to the right
    if ((hidden_right_cols > 0)) {
      animateCarousel('-=' + (column_width * hidden_right_cols));
    }

  }


  /**
   * Renders the list for the given children
   * @param array child_terms an array of terms, taken from the term_cache
   */
  function renderChildren(term, child_terms) {

    function createDataObject(terms_array) {
      // loop through each item to find out if they have children and build
      // out object of data for the template
      var data = $.map(terms_array, function (term) {
        return {
          'name': term,
          'hasChild': (term_cache[term].length > 0),
          'link': buildSubjectUrl(term),
          'count': term_counts[term]
        };
      });

      // console.log("render children data object: ", data);

      return data;
    }

    if (term === '/') {
      var term_count = term_counts['ROOT'];
    } else {
      var term_count = term_counts[term];
    }

    // create the column markup
    var markup = buildColumnMarkup({
      items: createDataObject(child_terms),
      level: term_stack.length, // level is 1-based, array length is 0-based
      view_all_link: buildSubjectUrl(term),
      view_all_total: term_count
    });

    // insert the markup into the doc
    $('.levels-position').append(markup);
    // console.log("children added to DOM");

    // enable the newly-added column
    turnColumnOn($('.level').last());
  }


  /**
   * Generates the breadcrumb template from the given items in the collection
   * tag stack
   */
  function generateBreadcrumbs() {
    // console.log("generateBreadcrumbs with term_stack = " + term_stack.join(" -> "));

    // transforms the stack of terms into an array of objects that look like this
    //   [
    //     { level: '', label: '' }
    //   ]
    function createDataObject(term_stack) {
      var data = $.map(term_stack, function (term, idx) {
        return {
          label: term,
          level: idx // level is 1-based
        }
      });

      // remove the first item ("/"); we don't need it in the breadcrumb
      data.shift();

      // console.log("breadcrumb data object (post-shift): ",  data);

      return data;
    }

    // create the breadcrumb markup
    var markup = buildBreadcrumbMarkup({
      items: createDataObject(term_stack)
    });

    // update the breadcrumb div with the new content
    $('.breadcrumb').replaceWith(markup);
  }


  // EVENT HANDLERS ==========================================================

  /**
   * Handles the click event on terms in columns (levels)
   */
  function handleTermClick(event) {
    // console.log(">>>> CLICK <<<<<")
    var clicked_el = $(event.target);

    // bail out if the term is a leaf node, so the link can go through normally
    if (clicked_el.hasClass('no-children')) {
      return true;
    }

    // cancel the event
    event.preventDefault();

    // determine what level we're at
    var clicked_level_num = clicked_el.data('level');

    // now show the term
    displayTerm(getTermFromElement(clicked_el), clicked_level_num);

    // update the 'active' state for parent/child terms
    var closest_level_el = clicked_el.closest('.level');
    closest_level_el.find('a').removeClass('active');
    clicked_el.addClass('active');
  }

  /**
   * Handles the click event on the breadcrumb links
   */
  function handleBreadcrumbClick(event) {
    // console.log(">>>> CLICK <<<<<")
    var clicked_el = $(event.target);

    // cancel the event
    event.preventDefault();

    // determine what level we're at
    var clicked_level_num = clicked_el.data('level');

    // pan the columns to allow us to show the children without animating
    // again later. basically, whatever it takes to get the clicked column to
    // the #2 pos.
    var left_pos = (clicked_level_num >= 2) ? ((clicked_level_num - 2) * -column_width) : 0;

    animateCarousel(left_pos);

    // if we're clicking on a term in the breadcrubm, we should not
    // remove the immediate children of the term, as that is what we'll be
    // showing. therefore increase the level num by 1.
    removeTermsAboveLevel(clicked_level_num + 1);

    // update breadcrumbs
    generateBreadcrumbs();

    // enable the last level
    turnColumnOn($('.level').last());
  }

  /**
   * Click handler for the left/right carousel buttons
   */
  function handleCarouselClick(event) {
    event.preventDefault();

    var clicked_el = $(event.target);
    // console.log(">>> carousel click " + clicked_el.className);

    // detect if the target has a class of active
    if (!clicked_el.hasClass('active')) {
      return false;
    }

    var operator = (clicked_el.hasClass('next')) ? '-=' : '+=';
    var delta = operator + $('.level').outerWidth(true);

    animateCarousel(delta);
  }

  /**
   * Handles scrolling the term list
   */
  function handleScrollColumnMousedown(event) {
    event.preventDefault();

    var el = $(event.target);
    var column = el.siblings('.level-scroll');
    // cache these properties since they're used multiple times
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
    column.stop();
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
    var normalized_delta = Math[(deltaY < 0 ? 'floor' : 'ceil')](deltaY/60);

    // multiply by 10 to move faster (1px at a time is not much fun)
    var distance_to_scroll = normalized_delta * 10;

    // console.log(delta, deltaY, distance_to_scroll);

    // set the scrollTop (i.e. - scroll the element)
    var scrollTop = $(this).scrollTop();
    $(this).scrollTop(scrollTop - distance_to_scroll);
  }


  // UI FUNCTIONS ============================================================

  /**
   * Pans the carousel left and right
   * @param  {string} delta The parameter that gets passed to the actual animate function, which is either number or an offset (+/-=)
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
    // get some metrics about our current display state
    var left_position = $('.levels-position').position().left;
    var current_depth = $('.levels-position .level').length;
    var hidden_left_cols = Math.abs(left_position / column_width);
    var hidden_right_cols = current_depth - 3 - hidden_left_cols;

    // console.log([
    // 	"POSITION AFTER ANIMATION",
    // 	"current_depth = " + current_depth,
    // 	"hidden_left_cols = " + hidden_left_cols,
    // 	"hidden_right_cols = " + hidden_right_cols
    // ].join("\n"));

    var prev_button = $el.find('.prev');
    var next_button = $el.find('.next');

    // show the left/prev arrow if we're panned right (shifted left) at all
    if (hidden_left_cols > 0) {
      prev_button.show().addClass('active');
    } else {
      prev_button.hide().removeClass('active');
    }

    // show the next arrow if right cols are out of view
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
  function turnColumnOff(el, requestor) {
    // clear timeout, for good measure (this may be redundant)
    clearTimeout(column_timeout);

    // always remove the hover class
    el.removeClass('level-active-over');

    // turn off the column
    if (
    // if it's not the last one
      (el.data('level') <= term_stack.length) ||
        // or if a lower column is trying to be active
        (requestor && (requestor.data('level') < el.data('level')))
      ) {
      el.removeClass('level-active');
      el.addClass('level-selection');
    }

    $last_column_active = null;

    // if there's no reequestor and the element is not equal to the last one, turn on the last one
    var last_col = $('.level').last();
    if (!requestor && (el !== last_col)) {
      turnColumnOn(last_col);
    }
  }

  function turnColumnOn(el) {
    // console.log("turnColumnOfflumnOn = ", el.data('level'), el.attr('class'));

    clearTimeout(column_timeout);

    // before enabling this column, turn off the other columns (only allow
    // one column active at a time)
    if (
    // don't try to turn off a column that doesn't exist
      $last_column_active !== null &&
        // don't try to turn off the column we're hovering over.
        // note: comapring levels seems to be the best bet. jquery elements
        // don't compare well
        ($last_column_active.data('level') !== el.data('level'))
      ) {
      turnColumnOff($last_column_active, el);
    }

    // turn it from a regular column into an active column
    el.removeClass('level-selection');
    el.addClass("level-active level-active-over");

    // mark this column as the last one active
    $last_column_active = el;
  }


  // UTLITY FUNCTIONS ========================================================

  function insertTaxonomyBrowserSkeleton() {
    var markup = [
      '<div id="taxonomy-browser" class="areas">',
      '<div class="wrapper">',
      '<p class="breadcrumb"></p>',
      '<div class="levels">',
      '<div class="levels-container cf">',
      '<div class="levels-position"></div>',
      '</div>',
      '<a href="#" class="prev"></a>',
      '<a href="#" class="next active"></a>',
      '</div>',
      '</div>',
      '</div>'
    ];

    // append our skeleton to the DOM
    $('#pagehdr-wrap').after(markup.join("\n"));
  }

  function buildColumnMarkup(data) {
    var terms = $.map(data.items, function (item, idx) {
      return [
        '<li>',
        '<a href="' + item.link + '"' + (item.hasChild ? '' : ' class="no-children"') + ' data-level="' + data.level + '">',
        item.name + (item.hasChild ? '' : ' (' + item.count + ')'),
        '</a>',
        '</li>'
      ].join("\n");
    });

    var markup = [
      '<div class="level" data-level="' + data.level + '">',
      '<div class="level-scroll">',
      '<ul>',
      '<li><a href="' + data.view_all_link + '" class="no-children">View All Articles (' + data.view_all_total + ')</a></li>',
      terms.join("\n"),
      '</ul>',
      '</div>' ];

    //We don't need the up / down arrows for less then 4 items
    //console.log(data.items.length);

    if(data.items.length > 4) {
      markup.push(['<a href="#" class="up"></a>', '<a href="#" class="down"></a>']);
    }

    markup.push(['</div>']);

    //console.log(markup.join("\n"));

    return markup.join("\n");
  }


  function buildBreadcrumbMarkup(data) {
    var crumbs = $.map(data.items, function (name, index) {
      return [
        '<span class="level' + name.level + '">',
        '<a href="' + buildSubjectUrl(name.label) + '" data-level="' + name.level + '">' + name.label + '</a>',
        (((index + 1) < data.items.length) ? " / " : ""),
        '</span>'
      ].join("\n");
    });

    var markup = [
      '<p class="breadcrumb">',
      '<strong>Browse Subject Areas:</strong>',
      crumbs.join("\n"),
      '</p>'
    ].join("\n");

    // console.log(markup);

    return markup;
  }

  /**
   * Build out the api string based on which items are in the collection
   *
   * @param string path_prefix URL prefix (including trailing slash)
   * @param string item An optional item to push on to end of collection
   */
  function buildAPIUrl(last_term) {
    // create a temp clone of the term_stack to build a URL with
    var all_terms = $.extend([], term_stack);

    // console.log("before: all_terms = ", all_terms);

    // add the last term to the array if we pass it in, ignoring root level
    // (trailing slash is already handled)
    if ((typeof(last_term) !== 'undefined') && (last_term !== '/')) {
      all_terms.push(last_term);
    }

    // remove the first item of the stack, which is always a "/"
    all_terms.shift();

    // console.log("after: all_terms = ", all_terms);

    // return an encoded version of the URL
    return API_URL + encodeURIComponent(all_terms.join("/"))
        + "&journal=" + $('meta[name=currentJournal]').attr("content")
        + "&showCounts=true";
  }

  /**
   * Build out the api string based on which items are in the collection
   *
   * @param string path_prefix URL prefix (including trailing slash)
   * @param string item An optional item to push on to end of collection
   */
  function buildSubjectUrl(last_term) {
    // create a temp clone of the term_stack to build a URL with
    var url = REST_URL;

    //Replace all spaces with "_" and encode the special characters
    if ((typeof(last_term) !== 'undefined') && (last_term !== '/')) {
      url = url + encodeURIComponent(last_term.replace(new RegExp("\\s", 'g'), "_").toLowerCase());
    }

    //console.log("Url = ", url);
    return url;
  }


  // get the term from the markup and trim it of whitespace.
  function getTermFromElement(el) {
    // FIXME: this will get more complicated if we need to deal with entities, etc.
    return $.trim(el.html());
  }

  /**
   * Makes API request for child terms for the given term
   */
  function loadTerms(parent_term) {

    function handleSuccess(data, textStatus, xhr) {
      // iterate over the returned terms, pulling out the child and
      // grandchild terms
      //
      // NOTE: 'terms' are referred to as 'categories' in the API response
      var child_terms = $.map(data.categories, function (grandchild_terms, term) {
        // store the grandchild terms in the cache. (no children will be
        // stored as an empty array)
        if (grandchild_terms.length > 0) {
          term_cache[term] = grandchild_terms;
        } else {
          term_cache[term] = [];
        }
        return term;
      });

      $.each(data.counts, function(k, v) {
        term_counts[k] = v;
      });

      // add the (sorted) child_terms for this parent to the cache
      term_cache[parent_term] = child_terms.sort();

      // console.log("JSON load successful");
    }

    function handleFailure(jqXHR, textStatus, errorThrown) {
      console.log("JSON load unsuccessful");

      //Disable the taxonomy browser
      $('#mn-01 a').unbind('click', displayBrowser);
      $('#mn-01 a').attr("title", "Error: No subject terms available!");
      $('#mn-01 a').addClass("disabled");

      $('#mn-01 a').on('click', function(e) {
        e.preventDefault();
        return false;
      });
    }

    // fetch the child terms for the given parent
    //
    // NOTE: async is set to false as we need the data immediately to render
    // the children due to the "has children" arrow (we can render the list
    // of terms, but we can't add the arrows without an additional request)
    $.ajax({
      url: buildAPIUrl(parent_term),
      type: 'GET',
      dataType: 'json',
      async: false
    }).then(handleSuccess, handleFailure);

    return true;
  }


  function removeTermsAboveLevel(dest_level) {
    // console.log("term_stack BEFORE removing to level " + dest_level + " " + term_stack.join(" -> "));

    // discard all levels deeper than we clicked, before we show any more children
    while ($('.levels .level').length > dest_level) {
      // discard the last list
      $('.levels .level').last().remove();

      // if ((term_stack.length - 1) > dest_level) { term_stack.pop(); }
      term_stack.pop();
    }

    // console.log("term_stack AFTER removing to level " + dest_level + " " + term_stack.join(" -> "));
  }


  function toggleTaxonomyBrowser(force_close) {
    // bail if we're already animating
    if ($tb.queue().length > 0) {
      return;
    }

    // set a sensible default
    force_close = force_close || false;

    // are we open?
    var tb_closed = $tb.is(':hidden');

    if (force_close || !tb_closed) {
      // optional slight optimization if no search pane; required if search
      // pane is present since the effects are run in parallel
      if (!tb_closed) {
        // close the TB and open the search pane, if it exists
        $tb.slideUp();
        $('#searchStripForm').slideDown();
      }
    } else {
      // open the TB and close the search pane, if it exists
      $tb.slideDown();
      $('#searchStripForm').slideUp();
    }
  }

  function displayBrowser(event) {
    if(typeof(_gaq) !== 'undefined'){
      _gaq.push(['_trackEvent',"Taxonomy Browser", "Browser Opened", ""]);
    }

    // stop the link
    event.preventDefault();

    // don't let the events get to 'document'
    event.stopPropagation();

    // toggle the TB
    toggleTaxonomyBrowser();
  }

  function attachEventHandlers() {

    // add hover states for the columns
    $('.level-active').hover(function () {
      $(this).addClass('level-active-over');
    }, function () {
      $(this).removeClass('level-active-over');
    });

    // attach an event to the main menu to show the TB
    $('#mn-01 a').on('click', displayBrowser);

    // any click outside the subject area close it
    $(document).bind('click', function (event) {
      // force close the TB
      toggleTaxonomyBrowser(true);

      if(typeof(_gaq) !== 'undefined'){
        _gaq.push(['_trackEvent',"Taxonomy Browser", "Browser Closed", ""]);
      }
    });

    // bind a listener to the TB parent node to prevents the closing
    // of the TB due to the event handler above
    $tb.click(function (event) {
      // don't let the events get to 'document'
      event.stopPropagation();
    });


    // cache this query (it's used a lot below)
    var $delegate = $('.levels-position');

    // no column hover events for touch devices
    if (!(('ontouchstart' in window) || window.DocumentTouch && document instanceof DocumentTouch)) {
      // turn column on immediately
      $delegate.on('mouseenter', '.level', function (e) {
        turnColumnOn($(e.currentTarget));
      });

      // wait 500ms before turning column off, to provide time for sloppy
      // mousing by user. note that we're not using hoverintent here as it
      // doesn't support this configuration.
      $delegate.on('mouseleave', '.level', function (e) {
        column_timeout = setTimeout(function () {
          turnColumnOff($(e.currentTarget));
        }, 500);
      });

      // scroll the list up and down
      // FIXME: should always scroll by a minimum amount
      $delegate.on('mousedown', '.level > a', handleScrollColumnMousedown);
      $delegate.on('mouseup', '.level > a', handleScrollColumnMouseup);
    }

    // all ui variants get the following events

    // scrollwheel support
    $delegate.on('mousewheel', '.level-scroll', handleMousewheel);

    // ???
    $delegate.on('click', '.level > a', function (e) {
      e.preventDefault();
    });

    // for clicking on terms in columns
    $delegate.on('click', 'ul li a', handleTermClick);

    // for clicking on the terms in the breadcrumb
    $('.wrapper').on('click', '.breadcrumb a', handleBreadcrumbClick);

    // for panning the carousel left and right (navigating up and down the hierarchy)
    $el.on('click', '.prev', handleCarouselClick);
    $el.on('click', '.next', handleCarouselClick);
  }

  // call us on DOM ready. (if we're included, we must be needed.)
  $(document).ready(function() {
    /**
     * Attach event handlers and kick off the rendering by making an API
     * request for the initial term list
     */

    // insert the required markup
    insertTaxonomyBrowserSkeleton();

    // grab a root-ish level element for later use
    // FIXME: this could probably be removed/refactored
    $el = $('.levels');

    // cache the ref to the TB root element
    $tb = $('#taxonomy-browser');

    // set up the event handlers so we can interact with it
    attachEventHandlers();

    // immediately get the root-level terms and render the children so it's
    // ready for action when the user reveals it. note: the hardcoded 0
    // refers to the "level" we're currently displaying.
    displayTerm('/', 0);

    // grab the column width for use later
    column_width = $('.levels-position .level').outerWidth(true);
  });
})();
