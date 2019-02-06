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

// Navigation - Contains javascript needed for searching and navigating through the site

var AmbraNavigation = function () {
  var self = this;
  var animation_time = '500';

  function buildAccordionItem($section) {
    // Find the title element from within the section
    var titleHtml = '';
    var $sectionTitle = null;
    $.each(['h1', 'h2', 'h3', 'h4', 'h5'], function (index, headlineType) {
      if (!$sectionTitle) {
        var $headline = $section.find(headlineType);
        if ($headline.size() > 0) {
          // Generally expect $headline.size() == 1, but default to using the first
          $sectionTitle = $($headline[0]);
        }
      }
    });

    // Remove the title element
    if ($sectionTitle) {
      titleHtml = $sectionTitle.html();
      $sectionTitle.remove();
    }

    // Build the accordion item
    var $accordionTitle = $('<a/>').html(titleHtml);
    var $accordionContent = $('<section/>').addClass('accordion-content');
    $accordionContent.append($section); // The title was removed; use what remains as the body
    var $accordionItem = $('<li/>').addClass('accordion-item');

    if (titleHtml.length > 0) {
      $accordionItem.append($accordionTitle); // include the title bar
      $accordionTitle.addClass('expander'); // enable expansion-toggling functionality
    } else {
      // The section has no title. Omit the title bar and open the section permanently.
      $accordionContent.show();
    }

    $accordionItem.append($accordionContent);
    return $accordionItem;
  }

  /**
   * Modify the page structure by setting up accordion sections.
   */
  self.build = function () {
    var $articleText = $('#articleText');
    var $accordionList = $('<ul/>').addClass('main-accordion').addClass('accordion');

    // Build a section combining 'abstract' and 'articleinfo'
    // These are two HTML sections in the transformed HTML, but we want them to be in one accordion item
    var $frontMatter = $();
    $.merge($frontMatter, $articleText.find('.abstract'));
    $.merge($frontMatter, $articleText.find('.articleinfo'));
    if ($frontMatter.size() > 0) {
      $accordionList.append(buildAccordionItem($frontMatter));
    }

    // Make an accordion item out of each regular article section
    var $sections = $articleText.find('div.section, div.acknowledgments, div.contributions, div.references');
    $sections.each(function (index) {
      $accordionList.append(buildAccordionItem($(this)));
    });

    // If there is text floating outside the other sections (anything non-whitespace not yet removed)
    if (!/^\s*$/.test($articleText.html())) {
      // Put it in a headerless section at the *top* of the article (before front matter)
      var $floatingTextSection = buildAccordionItem($articleText.clone());
      $accordionList.prepend($floatingTextSection);
    }

    // The first accordion item starts expanded
    var $accordionItems = $accordionList.children('.accordion-item');
    if ($accordionItems.size() > 0) {
      var $firstItem = $($accordionItems[0]);
      $firstItem.addClass('expanded');
      $firstItem.children('.accordion-content').show();
    }

    $articleText.html($accordionList);
  };

  /**
   * Initialize page elements.
   */
  self.init = function () {
    self.build();

    //globals
    self.$searchExpanded = $('.search-expanded');
    self.$searchButton = $('.site-search-button');
    self.$fullMenu = $('.full-menu-container');
    self.$siteMenuButton = $('#site-menu-button');
    self.$containerMain = $('#container-main');
    self.$containerMainOverlay = self.$containerMain.find('#container-main-overlay');
    self.modalOptions = { showModalOnMenuClose: false, displayMethod: null };

    //events
    self.$searchButton.click(function (e) {
      e.preventDefault();
      self.toggleSearch();
    });

    self.$siteMenuButton.click(function (e) {
      e.preventDefault();
      self.toggleMainMenu();
    });

    $('.accordion .accordion-item .expander').click(function (e) {
      e.preventDefault();
      self.toggleMainAccordion($(this));
    });

    $('.btn-top-container a.btn').click(function (e) {
      e.preventDefault();
      self.scrollToTop();
    });

    self.checkFixedSupport();

    // If the URL specifies a particular section via an anchor, expand that section.
    // TODO: generalize this if we need it for more than just the abstract.
    if (window.location.hash === '#s5') {
      self.toggleMainAccordion($('a.expander:contains("Abstract")'));
    }

    self.cleanMisplacedLinks();
  };

  self.toggleSearch = function () {
    var isActive = self.$searchExpanded.hasClass('active');
    if (isActive) { //deactivate the search box
      self.$searchButton.removeClass('active');
      self.$searchExpanded.removeClass('active');
      self.$searchExpanded.find('.search-field').val(""); //clear search field
    } else { //activate search box
      self.$searchButton.addClass('active');
      self.$searchExpanded.addClass('active');
      self.$searchExpanded.find('#search-execute').one('click', function (e) {
        self.executeSearch();
      });
      self.$searchExpanded.find('#search-cancel').one('click', function (e) { //trigger the toggle and remove the listener
        self.toggleSearch();
      });
    }
  };

  self.executeSearch = function () {
    var searchVal = self.$searchExpanded.find('#search-input').val();
    //PL-INT - implement search functionality here, using searchVal as the data entered into search field
  };

  self.toggleMainMenu = function () {
    var isInactive = self.$containerMain.hasClass('inactive');
    if (isInactive) { //if the main container is inactive, we should bring it back (activate it)
      self.hideMainMenu();
    } else { //show the site menu
      siteContentClass.findOpenModals(ambraNavigation.showMainMenu); //if any other modals are open we should close them
    }
  };

  self.hideMainMenu = function () {

    self.$containerMain.animate({'left': '0%' }, 300, function () {
      self.$containerMain.removeClass('inactive');
      self.$containerMainOverlay.removeClass('active'); //remove the overlay so that the site's main content can be interacted with again
      self.$fullMenu.hide();

      if (self.modalOptions.showModalOnMenuClose == true) { //if we are supposed to reactivate a modal now that the main menu is
        siteContentClass.showModalWindow(null, null, self.modalOptions.displayMethod);
      }

    });
  };

  self.showMainMenu = function () {
    self.$fullMenu.show();
    self.$containerMain.addClass('inactive');
    self.$containerMainOverlay.addClass('active'); //prevent the content from being interacted with when site menu is open
    self.$containerMain.animate({'left': '80%' }, 300);
  };

  // Accordion menus which appear in the body of the site
  self.toggleMainAccordion = function ($activeAccordion, $scroll_target) {
    var $accordionListItem = $activeAccordion.parent('li');
    // collapse this accordion
    if ($accordionListItem.hasClass('expanded')) {
      $accordionListItem.removeClass('expanded');
      $accordionListItem.children('.accordion-content').slideUp(animation_time);

    } else {
      // first collapse any open accordions, and then show this one.
      $accordionListItem.addClass('expanded');
      $accordionListItem.children('.accordion-content').slideDown(animation_time,
        // if there is a scroll target passed then
       function(){ if ($scroll_target){
         $('html, body').animate({
           scrollTop: $scroll_target.offset().top
         }, 500);
         }
        }

      );
    }
  };

  self.togglePeerReviewAccordion = function (expandLink) {
    var accordionItem = expandLink.parents(".peer-review-accordion-item");
    var accordionContent = accordionItem.find('.peer-review-accordion-content');
    if (accordionItem.hasClass('expanded')) {
          accordionItem.removeClass('expanded');
          accordionContent.slideUp(animation_time);
        } else {
          accordionItem.addClass('expanded');
          accordionContent.slideDown(animation_time);
        }
  };

  $('.peer-review-accordion-expander').click(function (e) {
    e.preventDefault();
    self.togglePeerReviewAccordion($(this));
  });

  self.scrollToTop = function () {
    if (Modernizr.android && !Modernizr.positionfixed) { //account for an infrequent Android < 3 bug where window animates to the top, but then jumps back to the bottom
      $('body').scrollTop(0);
    } else {
      $("html, body").animate({ scrollTop: 0 }, 300);
    }

  };

  self.checkFixedSupport = function () { //see if the browser supports fixed positioning for scrolling modals

    //alert (navigator.userAgent);

    Modernizr.addTest('ipad', function () {
      return !!navigator.userAgent.match(/iPad/i);
    });

    Modernizr.addTest('iphone', function () {
      return !!navigator.userAgent.match(/iPhone/i);
    });

    Modernizr.addTest('ipod', function () {
      return !!navigator.userAgent.match(/iPod/i);
    });

    Modernizr.addTest('appleios', function () {
      return (Modernizr.ipad || Modernizr.ipod || Modernizr.iphone);
    });

    Modernizr.addTest('android', function () {
      return !!navigator.userAgent.match(/Android/i);
    });


    Modernizr.addTest('positionfixed', function () {
      /* iOS < 5 & Android < 3 would return a false positive here.
       * If it's about to return true, explicitly test for known User Agent strings.
       * positionfixed is on Modernizr's list of undectables, so we must do this currently */

      navigator.userAgent.match(/OS (\d)/i);
      if (Modernizr.appleios && RegExp.$1 < 5) {
        return false;
      }

      navigator.userAgent.match(/Android (\d)/i);
      if (Modernizr.android && RegExp.$1 < 3) {
        return false;
      }

      var test = document.createElement('div'),
        control = test.cloneNode(false),
        fake = false,
        root = document.body || (function () {
          fake = true;
          return document.documentElement.appendChild(document.createElement('body'));
        }());

      var oldCssText = root.style.cssText;
      root.style.cssText = 'padding:0;margin:0';
      test.style.cssText = 'position:fixed;top:42px';
      root.appendChild(test);
      root.appendChild(control);
      var ret = test.offsetTop !== control.offsetTop;

      root.removeChild(test);
      root.removeChild(control);
      root.style.cssText = oldCssText;
      if (fake) {
        document.documentElement.removeChild(root);
      }

      return ret;
    });

  }

  // Kludge to clean up misplaced links taken from article text that point to something not on this page
  // TODO: Obviate this
  self.cleanMisplacedLinks = function () {
    $('.figure-description a').each(function (index, link) {
      var $link = $(link);
      var href = $link.attr('href');

      // If it's a link to an anchor on the same page
      if (href && href.charAt(0) == '#') {
        // Remove the <a> element but keep the text inside
        $link.contents().unwrap();
      }
    });
  };

};

var ambraNavigation;

$(document).ready(function () {
  ambraNavigation = new AmbraNavigation();
  ambraNavigation.init();
});
