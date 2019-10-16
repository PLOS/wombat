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

// SUBJECT DROPDOWN
$(function () {

  $('.filter-bar.subject ul li.first').hoverIntent({
    over: function () {
      $(this).addClass('over');
    },
    timeout: 500,
    out: function () {
      $(this).removeClass('over');
    }
  });
});

// ARTICLE BLOCKS
$(function () {

  // get all article blocks in document
  var $article_blocks = $('.article-block');

  // bail if no article blocks exist
  if ($article_blocks.length < 1) {
    return;
  }

/*  // add the grayscale image
  $('.article-block > a > img').filterize();*/
  $('.article-block > a > img').load(function() {
    $('div img').show();
  });

  // for each of the tabs, fix the article blocks within them
  $('.articles-list').each(function () {
    // get article blocks in current list
    var $blocks = $(this).children('.article-block');

    // add some helper classes to enable layout. this is done via JS so that 
    // it works in all browsers that can't do advanced CSS pseudo-selectors.
    //
    // 1) twitter and ads are to the right here
    $blocks.eq(2).addClass('clear');
    $blocks.eq(4).addClass('clear');

    // 2) remove the right margin so they fit into layout 3 across
    for(var a = 6; a < $blocks.size(); a = a + 3) {
      $blocks.eq(a).addClass('article-block-last');
    }
  });

});


// ARTICLES LIST "MORE" BUTTON
$(function () {

  function displayMoreArticles(data) {
    var el = $(this);

    // the data returned is a markup fragment
    var content = $(data);

    // every 3rd element needs special class to remove right margin for layout purposes
    content.filter('.article-block:nth-child(3n)').addClass('article-block-last');

    // hide the loading spinner
    $('.articles-list .loader').remove();

    // append new items to the articles-list div
    el.closest('.articles-list').append(content);
    el.closest('.articles-list').find('a.abstract').on('click', function (e) {
      doi = $(this).data('doi');
      FigViewerInit(doi, null, 'abst', true);
      e.preventDefault();
      return false;
    });

    el.closest('.articles-list').find('a.figures').on('click', function (e) {
      doi = $(this).data('doi');
      FigViewerInit(doi, null, 'figs', true);
      e.preventDefault();
      return false;
    });

    // add the grayscale image
    // use a specific selector so that it targets images that have not been 
    // processed yet.
    $('.article-block > a > img').filterize();

    // remove the current more button, as the new button should be in the ajax
    // call with the new url
    el.closest('.pagination').remove();
  }


  function loadMoreArticles(more_link) {
    // get the href from the "more" link
    var url = more_link.attr('href');

    // hide the more link
    more_link.hide();

    // make the current link disabled
    more_link.attr('disabled', true);

    // display animation that shows content is loading
    var loader = $('<div/>').attr('class', 'loader')
      .append($('<img/>').attr('src', '../img/loading-spinner.gif'))
      .insertBefore(more_link.closest('.pagination'));

    // load data via xhr
    $.ajax({
      url: url,
      type: 'GET',
      dataType: 'text',
      success: function (data, textStatus, xhr) {
        $.proxy(displayMoreArticles, more_link)(data);
      },
      error: function (xhr, textStatus, errorThrown) {
        // re-enable the button
        more_link.attr('disabled', false);

        // remove the loading spinner
        $('.articles-list .loader').remove();
      }
    });
  }

  function handleMoreClick(event) {
    event.preventDefault();

    // get the event target
    var more_link = $(event.target);

    // if the link is disabled, we don't want to move forward
    if (more_link.attr('disabled')) {
      return;
    }

    loadMoreArticles(more_link);
  }

  // attach the event handler via a delegated event
  $('.articles-list').on('click', '.more', handleMoreClick);

});

// STICKY FOOTER
$(function () {

  var footer = $('#sticky-footer');

  function showDiv() {
    var threshold = 100;

    if (
      ($(window).scrollTop() > threshold) &&
        (footer.data('positioned') == 'false')
      ) {
      footer.fadeIn().data('positioned', 'true');

    } else if (
      ($(window).scrollTop() <= threshold) &&
        (footer.data('positioned') == 'true')
      ) {
      footer.fadeOut().data('positioned', 'false');
    }
  }

  $(window).scroll(showDiv);

  // update the data element to keep track of the state
  footer.data('positioned', 'false');

  // attach the event handler for closing it
  $('a.close').click(function (event) {
    event.preventDefault();

    footer.fadeOut(function () {
      footer.css('visibility', 'hidden');
    });
  });

});


// HANDLES THE BROWSER HISTORY AND PAGE LOAD EVENT
$(function () {

  // bail if there's not more than one 'tab' in the page
  //
  // FIXME: need a better way to determine if we should run on a page. maybe
  // include this JS inline w/ markup?
  if ($('.articles-list').length <= 1) {
    return;
  }

  function showTab(class_name) {
    // console.log('showing tab ' + class_name);

    // hide all other lists; show this list
    // NOTE: 'offscreen' class keeps elements in doc flow for dimension 
    // calculation, necessary for grayscale effect.
    $('.articles-list').addClass('offscreen');
    $('.articles-list.' + class_name).removeClass('offscreen');

    // make the previously active item inactive; make the clicked item active
    $('.filter-bar .active').removeClass('active');
    $(this).addClass('active');
  }

  function readLocationAndGotoTab() {
    // get the hash from the url
    var hash = window.location.hash.replace('#', '');

    // if hash is empty, set hash to the default (news)
    if (hash == '') {
      hash = "news";
    }

    // activate the tab
    // FIXME: this assumes the tab exists. if a bad hash is passed in, then
    // we end up hiding everything and showing nothing.
    $.proxy(showTab, $('.filter-bar .' + hash))(hash);
  }

  function navigateToTab(clicked_el) {
    // get the class name from the clicked element
    // FIXME: this feels slightly weird. bad way to get class we need, no?
    var class_name = clicked_el.attr('class').split(' ').pop();

    // if we are not already on this tab, handle the tab show/hide event
    if (!clicked_el.hasClass('active')) {
      // change the URL if there's support for it
      if (window.history && history.pushState) {
        // console.log('pushing state...');
        history.pushState(null, null, "#" + class_name);
      }

      // activate the tab
      $.proxy(showTab, clicked_el)(class_name);
    }
  }


  function handlePopstate() {
    // console.log('>>> handlePopstate <<<');

    // really only needed once for firefox bug, but harmless to set all the time.
    popstate_fired = true;

    // figure out where to go and go there
    readLocationAndGotoTab();
  }

  function handleTabClick(event) {
    // console.log('>>> handleTabClick <<<');

    // prevent navigation to URL
    event.preventDefault();

    // navigate to clicked tab
    navigateToTab($(event.target));
  }


  // hide the 'popular' and 'recent' tab content on load of the page
  $('.articles-list.popular, .articles-list.recent').addClass('offscreen');

  // attach tab click event handler
  $('.filter-bar .recent, .filter-bar .popular, .filter-bar .news').on('click', handleTabClick);

  // flag for firefox, see below
  var popstate_fired = false;

  // if there's history support, listen for popstate events
  if (window.history && history.pushState) {
    // console.log('history support enabled');

    $(window).on('popstate', handlePopstate);

    // patch for firefox, as it does not fire popstate event on load of page
    $(window).on('load', function () {
      // console.log('window onload event. checking popstate_fired');
      if (!popstate_fired) {
        // console.log('popstate not fired yet... running handlePopstate');
        handlePopstate();
      } else {
        // console.log('already fired. all good.');
      }
    });

    // otherwise, just go where the user wants to go
  } else {
    // console.log('no history support. :-(');
    readLocationAndGotoTab();
  }

});


// FOOTER CAROUSEL
$(function () {

  // bail if there is no carousel
  if ($('#footer-slides').length < 1) {
    return;
  }

  // cache the array of slides for later use
  var $slides = $('#footer-slides .slide');

  // store this; may need it later
  var current_slide = 1;

  // build and set up navigation elements (prev/next + control nav)
  buildNav(current_slide);

  // set up event handlers for navigation
  $('#footer-slides').on('click', '.slide-nav a', handleNavClick);
  $('#footer-slides').on('click', '.control-nav a', handleNavClick);

  $('.slide-nav').hover(function () {
    $(this).children('a').addClass('over');
  }, function () {
    $(this).children('a').removeClass('over');
  });

  // prepare layout for animation
  var slide_height = $slides.height();
  $('.slides').css({
    'height': (slide_height + 'px')
  })

  // show navigation
  $('#footer-slides .slide-nav').show();
  $('#footer-slides .control-nav').show();

  // event handler for all navigation clicks
  function handleNavClick(event) {
    event.preventDefault();

    // grab the dest slide num from the data attr
    var slide_num = $(event.currentTarget).data('dest');
    showSlide(slide_num);
  }

  // util functions
  function showSlide(slide_num) {
    // animate slides
    var slide_width = $slides.width();
    $('.slides-scroller').animate({
      'left': ((slide_num - 1) * -slide_width) // use zero-based num for position caluclation
    })

    // reflect new slide in navigation states
    updateNavButtons(slide_num);

    current_slide = slide_num;
  }

  function determineNextPrevSlideNums(slide_num) {
    var next_slide_num, pre_slide_num;

    if (slide_num >= $slides.length) {
      next_slide_num = 1;
      prev_slide_num = slide_num - 1;

    } else if (slide_num <= 1) {
      next_slide_num = slide_num + 1;
      prev_slide_num = $slides.length;

    } else {
      next_slide_num = slide_num + 1;
      prev_slide_num = slide_num - 1;
    }

    return {
      'next': next_slide_num,
      'prev': prev_slide_num
    };
  }

  function updateNavButtons(slide_num) {
    // update control nav state, removing/adding 'active' class
    $('.control-nav a.active').removeClass('active');
    $('.control-nav li:nth-child(' + (slide_num) + ') a').addClass('active'); // nth child is zero-based

    // update content of prev/next buttons
    var next_prev = determineNextPrevSlideNums(slide_num);
    var $next_slide = $slides.eq(next_prev['next'] - 1); // eq() is zero-based
    var $prev_slide = $slides.eq(next_prev['prev'] - 1);

    // replace markup of next/prev slide buttons with totally new markup
    $('.next-slide').html(buildNextPrevButtonMarkup($next_slide, next_prev['next']));
    $('.prev-slide').html(buildNextPrevButtonMarkup($prev_slide, next_prev['prev']));
  }

  function buildNav(slide_num) {
    var next_prev = determineNextPrevSlideNums(slide_num);
    var $next_slide = $slides.eq(next_prev['next'] - 1); // eq() is zero-based
    var $prev_slide = $slides.eq(next_prev['prev'] - 1);

    // update content of prev/next buttons + control nav
    var $next_slide_nav = $('<div class="next-slide slide-nav"><\/div>').append(
      buildNextPrevButtonMarkup($next_slide, next_prev['next'])
    );

    var $prev_slide_nav = $('<div class="prev-slide slide-nav"><\/div>').append(
      buildNextPrevButtonMarkup($prev_slide, next_prev['prev'])
    );

    var $control_nav = $('<ol class="control-nav"><\/ol>').append(
      buildControlNavMarkup($slides.length, 1)
    );

    // append all navs to #footer-slides
    $("#footer-slides").append($next_slide_nav).append($prev_slide_nav).append($control_nav);
  }

  function buildNextPrevButtonMarkup($slide, dest) {
    var markup = [
      '<a href="#" data-dest="' + dest + '">',
      '<span class="row">',
      '<span class="cell image"><img src="' + $slide.data('small-img') + '" alt="" /></span>',
      '<span class="cell">' + $slide.data('short-title') + '</span>',
      '</span>',
      '</a>'
    ];

    return $(markup.join("\n"));
  }

  function buildControlNavMarkup(num_slides, active_slide_num) {
    var markup = [];

    active_slide_num = active_slide_num || 0;

    for (var i = 1; i <= num_slides; i++) {
      markup.push(
        '<li>' +
          '<a href="#" data-dest="' + i + '" class="' + ((active_slide_num == i) ? "active" : "") + '">' +
          i +
          '<\/a>' +
          '<\/li>'
      );
    }

    return $(markup.join("\n"));
  }

});
