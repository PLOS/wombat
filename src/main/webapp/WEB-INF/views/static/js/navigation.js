// Navigation - Contains javascript needed for searching and navigating through the site

var AmbraNavigation = function () {
  var self = this;

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
    var $accordionTitle = $('<a/>').addClass('expander').text(titleHtml);
    var $accordionContent = $('<section/>').addClass('accordion-content');
    $accordionContent.append($section); // The title was removed; use what remains as the body
    var $accordionItem = $('<li/>').addClass('accordion-item');
    $accordionItem.append($accordionTitle);
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

    $articleText.html($accordionList);
  }

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
    self.$searchButton.on('click', function (e) {
      e.preventDefault();
      self.toggleSearch();
    });

    self.$siteMenuButton.on('click', function (e) {
      e.preventDefault();
      self.toggleMainMenu();
    });

    $('.accordion .accordion-item .expander').on('click', function (e) {
      e.preventDefault();
      self.toggleMainAccordion($(this));
    });

    $('.btn-top-container a.btn').on('click', function (e) {
      e.preventDefault();
      self.scrollToTop();
    });

    self.checkFixedSupport();

  }

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
  }

  self.executeSearch = function () {
    var searchVal = self.$searchExpanded.find('#search-input').val();
    //PL-INT - implement search functionality here, using searchVal as the data entered into search field
  }

  self.toggleMainMenu = function () {
    var isInactive = self.$containerMain.hasClass('inactive');
    if (isInactive) { //if the main container is inactive, we should bring it back (activate it)
      self.hideMainMenu();
    } else { //show the site menu
      siteContentClass.findOpenModals(ambraNavigation.showMainMenu); //if any other modals are open we should close them
    }
  }

  self.hideMainMenu = function () {

    self.$containerMain.animate({'left': '0%' }, 300, function () {
      self.$containerMain.removeClass('inactive');
      self.$containerMainOverlay.removeClass('active'); //remove the overlay so that the site's main content can be interacted with again
      self.$fullMenu.hide();

      if (self.modalOptions.showModalOnMenuClose == true) { //if we are supposed to reactivate a modal now that the main menu is
        siteContentClass.showModalWindow(null, null, self.modalOptions.displayMethod);
      }

    });
  }

  self.showMainMenu = function () {
    self.$fullMenu.show();
    self.$containerMain.addClass('inactive');
    self.$containerMainOverlay.addClass('active'); //prevent the content from being interacted with when site menu is open
    self.$containerMain.animate({'left': '80%' }, 300);
  }

  self.toggleMainAccordion = function ($activeAccordion) { //Accordion menus which appear in the body of the site

    var $accordionListItem = $activeAccordion.parent('li');
    var $accordionList = $accordionListItem.parent('ul');

    var isExpanded = $accordionListItem.hasClass('expanded');

    if (isExpanded) { //collapse this accordion
      $accordionListItem.removeClass('expanded');
      $accordionListItem.children('.accordion-content').slideUp(500);
    } else { //show this accordion
      $accordionListItem.addClass('expanded');
      $accordionListItem.children('.accordion-content').slideDown(500);
    }

  }

  self.scrollToTop = function () {

    if (Modernizr.android && !Modernizr.positionfixed) { //account for an infrequent Android < 3 bug where window animates to the top, but then jumps back to the bottom
      $('body').scrollTop(0);
    } else {
      $("html, body").animate({ scrollTop: 0 }, 300);
    }

  }

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

}

var ambraNavigation;

$(document).ready(function () {
  ambraNavigation = new AmbraNavigation();
  ambraNavigation.init();
});
